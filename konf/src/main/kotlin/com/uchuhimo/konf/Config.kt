package com.uchuhimo.konf

import com.uchuhimo.collections.mutableBiMapOf
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ConfigGetter {
    operator fun <T : Any> get(item: Item<T>): T
    operator fun <T : Any> get(name: String): T
    fun <T : Any> getOrNull(item: Item<T>): T?
    fun <T : Any> getOrNull(name: String): T?
    operator fun <T : Any> invoke(name: String): T = get(name)
}

interface Config : ConfigGetter {
    operator fun iterator(): Iterator<Item<*>>
    operator fun contains(item: Item<*>): Boolean
    operator fun contains(name: String): Boolean
    fun rawSet(item: Item<*>, value: Any)
    operator fun <T : Any> set(item: Item<T>, value: T)
    operator fun <T : Any> set(name: String, value: T)
    fun <T : Any> lazySet(item: Item<T>, lazyThunk: (ConfigGetter) -> T)
    fun <T : Any> lazySet(name: String, lazyThunk: (ConfigGetter) -> T)
    fun unset(item: Item<*>)
    fun unset(name: String)

    fun <T : Any> property(item: Item<T>): ReadWriteProperty<Any?, T>
    fun <T : Any> property(name: String): ReadWriteProperty<Any?, T>

    val name: String
    val parent: Config?
    val items: List<Item<*>> get() = mutableListOf<Item<*>>().apply {
        addAll(this@Config.iterator().asSequence())
    }

    val specs: List<ConfigSpec>
    fun addSpec(spec: ConfigSpec)
    fun withLayer(name: String = ""): Config

    @Suppress("NAME_SHADOWING", "UNUSED_ANONYMOUS_PARAMETER")
    fun visitAsTree(
            onEnterNode: (List<String>) -> Unit = { path -> },
            onLeaveNode: (List<String>) -> Unit = { path -> },
            onEnterLeaf: (List<String>, Item<*>) -> Unit = { path, item -> })

    companion object {
        operator fun invoke(): Config = ConfigImpl()

        operator fun invoke(init: Config.() -> Unit): Config = Config().apply(init)
    }
}

class RepeatedItemException(message: String) : Exception(message)

class NameConflictException(message: String) : Exception(message)

class InvalidLazySetException(message: String) : Exception(message)

private class ConfigImpl private constructor(
        override val name: String,
        override val parent: ConfigImpl?
) : Config {
    constructor() : this("", null)

    private val specsInLayer = mutableListOf<ConfigSpec>()
    private val valueByItem = mutableMapOf<Item<*>, ValueState>()
    private val nameByItem = mutableBiMapOf<Item<*>, String>()
    private val tree: ConfigTree = run {
        if (parent != null) {
            parent.tree.deepCopy()
        } else {
            ConfigNode(path = emptyList(), children = mutableListOf())
        }
    }
    private var hasChildren = false

    private val lock = ReentrantReadWriteLock()

    override fun iterator(): Iterator<Item<*>> = object : Iterator<Item<*>> {
        var currentConfig = this@ConfigImpl
        var current = currentConfig.nameByItem.keys.iterator()

        override tailrec fun hasNext(): Boolean {
            if (current.hasNext()) {
                return true
            } else {
                val parent = currentConfig.parent
                if (parent != null) {
                    currentConfig = parent
                    current = currentConfig.nameByItem.keys.iterator()
                    return hasNext()
                } else {
                    return false
                }
            }
        }

        override fun next(): Item<*> = current.next()
    }

    override fun visitAsTree(
            onEnterNode: (List<String>) -> Unit,
            onLeaveNode: (List<String>) -> Unit,
            onEnterLeaf: (List<String>, Item<*>) -> Unit) {
        tree.visit(
                onLeaveNode = { node -> onLeaveNode(node.path) },
                onEnterNode = { node -> onEnterNode(node.path) },
                onEnterLeaf = { leaf -> onEnterLeaf(leaf.path, leaf.item) })
    }

    override fun <T : Any> get(item: Item<T>): T = getOrNull(item, errorWhenUnset = true) ?:
            throw NoSuchElementException("cannot find ${item.name} in config")

    override fun <T : Any> get(name: String): T = getOrNull<T>(name, errorWhenUnset = true) ?:
            throw NoSuchElementException("cannot find $name in config")

    override fun <T : Any> getOrNull(item: Item<T>): T? =
            getOrNull(item, errorWhenUnset = false)

    private fun <T : Any> getOrNull(
            item: Item<T>,
            errorWhenUnset: Boolean,
            lazyContext: ConfigGetter = this
    ): T? {
        val valueState = lock.read { valueByItem[item] }
        if (valueState != null) {
            @Suppress("UNCHECKED_CAST")
            return when (valueState) {
                is ValueState.Unset ->
                    if (errorWhenUnset) {
                        error("${item.name} is unset")
                    } else {
                        return null
                    }
                is ValueState.Value -> valueState.value as T
                is ValueState.Lazy<*> -> {
                    val value = valueState.thunk(lazyContext)!!
                    if (item.type.rawClass.isInstance(value)) {
                        value as T
                    } else {
                        throw InvalidLazySetException(
                                "fail to cast $value with ${value::class} to ${item.type.rawClass}" +
                                        " when getting ${item.name} in config")
                    }
                }
            }
        } else {
            if (parent != null) {
                return parent.getOrNull(item, errorWhenUnset, lazyContext)
            } else {
                return null
            }
        }
    }

    private fun getItemOrNull(name: String): Item<*>? {
        val item = lock.read { nameByItem.inverse[name] }
        if (item != null) {
            return item
        } else {
            if (parent != null) {
                return parent.getItemOrNull(name)
            } else {
                return null
            }
        }
    }

    override fun <T : Any> getOrNull(name: String): T? = getOrNull(name, errorWhenUnset = false)

    private fun <T : Any> getOrNull(name: String, errorWhenUnset: Boolean): T? {
        val item = getItemOrNull(name) ?: return null
        @Suppress("UNCHECKED_CAST")
        return getOrNull(item as Item<T>, errorWhenUnset)
    }

    override fun contains(item: Item<*>): Boolean {
        if (lock.read { valueByItem.containsKey(item) }) {
            return true
        } else {
            if (parent != null) {
                return parent.contains(item)
            } else {
                return false
            }
        }
    }

    override fun contains(name: String): Boolean {
        if (lock.read { nameByItem.containsValue(name) }) {
            return true
        } else {
            if (parent != null) {
                return parent.contains(name)
            } else {
                return false
            }
        }
    }

    override fun rawSet(item: Item<*>, value: Any) {
        if (item.type.rawClass.isInstance(value)) {
            if (item in this) {
                lock.write {
                    val valueState = valueByItem[item]
                    if (valueState is ValueState.Value) {
                        valueState.value = value
                    } else {
                        valueByItem[item] = ValueState.Value(value)
                    }
                }
            } else {
                throw NoSuchElementException("cannot find ${item.name} in config")
            }
        } else {
            throw ClassCastException(
                    "fail to cast $value with ${value::class} to ${item.type.rawClass}" +
                            " when setting ${item.name} in config")
        }
    }

    override fun <T : Any> set(item: Item<T>, value: T) {
        rawSet(item, value)
    }

    override fun <T : Any> set(name: String, value: T) {
        val item = getItemOrNull(name)
        if (item != null) {
            @Suppress("UNCHECKED_CAST")
            set(item as Item<T>, value)
        } else {
            throw NoSuchElementException("cannot find $name in config")
        }
    }

    override fun <T : Any> lazySet(item: Item<T>, lazyThunk: (ConfigGetter) -> T) {
        if (item in this) {
            lock.write {
                val valueState = valueByItem[item]
                if (valueState is ValueState.Lazy<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (valueState as ValueState.Lazy<T>).thunk = lazyThunk
                } else {
                    valueByItem[item] = ValueState.Lazy(lazyThunk)
                }
            }
        } else {
            throw NoSuchElementException("cannot find ${item.name} in config")
        }
    }

    override fun <T : Any> lazySet(name: String, lazyThunk: (ConfigGetter) -> T) {
        val item = getItemOrNull(name)
        if (item != null) {
            @Suppress("UNCHECKED_CAST")
            lazySet(item as Item<T>, lazyThunk)
        } else {
            throw NoSuchElementException("cannot find $name in config")
        }
    }

    override fun unset(item: Item<*>) {
        if (item in this) {
            lock.write { valueByItem[item] = ValueState.Unset }
        } else {
            throw NoSuchElementException("cannot find ${item.name} in config")
        }
    }

    override fun unset(name: String) {
        val item = getItemOrNull(name)
        if (item != null) {
            unset(item)
        } else {
            throw NoSuchElementException("cannot find $name in config")
        }
    }

    override fun <T : Any> property(item: Item<T>): ReadWriteProperty<Any?, T> {
        if (!contains(item)) {
            throw NoSuchElementException("cannot find ${item.name} in config")
        }
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T = get(item)

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
                    set(item, value)
        }
    }

    override fun <T : Any> property(name: String): ReadWriteProperty<Any?, T> {
        if (!contains(name)) {
            throw NoSuchElementException("cannot find $name in config")
        }
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T = get(name)

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
                    set(name, value)
        }
    }

    private tailrec fun addNode(tree: ConfigTree, path: List<String>, item: Item<*>) {
        when (tree) {
            is ConfigNode -> {
                if (path.isEmpty()) {
                    throw NameConflictException("${item.name} cannot be added" +
                            " since the following items has been added to config:" +
                            " ${tree.items.joinToString { it.name }}")
                }
                val matchChild = tree.children.find { it.path.last() == path[0] }
                if (matchChild != null) {
                    addNode(matchChild, path.drop(1), item)
                } else {
                    if (path.size == 1) {
                        tree.children += ConfigLeaf(tree.path + path[0], item)
                    } else {
                        val child = ConfigNode(tree.path + path[0], mutableListOf())
                        tree.children += child
                        addNode(child, path.drop(1), item)
                    }
                }
            }
            is ConfigLeaf<*> -> {
                if (path.isEmpty()) {
                    throw NameConflictException("item ${item.name} has been added")
                } else {
                    throw NameConflictException("${item.name} cannot be added" +
                            " since item ${tree.item.name} has been added to config")
                }
            }
        }
    }

    override val specs: List<ConfigSpec> get() = mutableListOf<ConfigSpec>().apply {
        addAll(object : Iterator<ConfigSpec> {
            var currentConfig = this@ConfigImpl
            var current = currentConfig.specsInLayer.iterator()

            override tailrec fun hasNext(): Boolean {
                if (current.hasNext()) {
                    return true
                } else {
                    val parent = currentConfig.parent
                    if (parent != null) {
                        currentConfig = parent
                        current = currentConfig.specsInLayer.iterator()
                        return hasNext()
                    } else {
                        return false
                    }
                }
            }

            override fun next(): ConfigSpec = current.next()
        }.asSequence())
    }

    override fun addSpec(spec: ConfigSpec) {
        lock.write {
            if (hasChildren) {
                throw UnsupportedOperationException(
                        "this config has children layer, cannot add new spec")
            }
            spec.items.forEach { item ->
                val name = item.name
                if (item !in this) {
                    if (name !in this) {
                        addNode(tree, item.path, item)
                        nameByItem[item] = name
                        valueByItem[item] = when (item) {
                            is OptionalItem -> ValueState.Value(item.default)
                            is RequiredItem -> ValueState.Unset
                            is LazyItem -> ValueState.Lazy(item.thunk)
                        }
                    } else {
                        throw NameConflictException("item $name has been added")
                    }
                } else {
                    throw RepeatedItemException("item $name has been added")
                }
            }
            specsInLayer += spec
        }
    }

    override fun withLayer(name: String): Config {
        lock.write { hasChildren = true }
        return ConfigImpl(name, this)
    }

    private sealed class ValueState {
        object Unset : ValueState()
        data class Lazy<T>(var thunk: (ConfigGetter) -> T) : ValueState()
        data class Value(var value: Any) : ValueState()
    }
}
