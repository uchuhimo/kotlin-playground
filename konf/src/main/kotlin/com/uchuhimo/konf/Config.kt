package com.uchuhimo.konf

import com.uchuhimo.collections.mutableBiMapOf
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

interface ConfigGetter {
    operator fun <T : Any> get(item: Item<T>): T
    operator fun <T : Any> get(name: String): T
    fun <T : Any> getOrNull(item: Item<T>): T?
    fun <T : Any> getOrNull(name: String): T?
    operator fun <T : Any> invoke(name: String): T = get(name)
}

interface Config : ConfigGetter {
    operator fun <T : Any> set(item: Item<T>, value: T)
    operator fun <T : Any> set(name: String, value: T)
    operator fun contains(item: Item<*>): Boolean
    operator fun contains(name: String): Boolean

    fun <T : Any> property(item: Item<T>): ReadWriteProperty<Any?, T>
    fun <T : Any> property(name: String): ReadWriteProperty<Any?, T>

    val name: String
    val parent: Config?
    fun addSpec(spec: ConfigSpec)
    fun withLayer(name: String = ""): Config

    companion object {
        operator fun invoke(): Config = ConfigImpl()
        operator fun invoke(init: Config.() -> Unit): Config = Config().apply(init)
    }
}

class RepeatedItemException(message: String) : Exception(message)

class RepeatedNameException(message: String) : Exception(message)

private class ConfigImpl private constructor(
        override val name: String,
        override val parent: ConfigImpl?
) : Config {
    constructor() : this("", null)

    private val valueByItem = mutableMapOf<Item<*>, ValueState>()
    private val nameByItem = mutableBiMapOf<Item<*>, String>()

    private val lock = ReentrantReadWriteLock()

    override fun <T : Any> get(item: Item<T>): T = getOrNull(item) ?:
            throw NoSuchElementException("cannot find ${item.name} in config")

    override fun <T : Any> get(name: String): T = getOrNull<T>(name) ?:
            throw NoSuchElementException("cannot find $name in config")

    override fun <T : Any> getOrNull(item: Item<T>): T? = getOrNull(item, this)

    private fun <T : Any> getOrNull(item: Item<T>, lazyContext: ConfigGetter): T? {
        val valueState = lock.read { valueByItem[item] }
        if (valueState != null) {
            @Suppress("UNCHECKED_CAST")
            return when (valueState) {
                is ValueState.Unset -> error("${item.name} is unset")
                is ValueState.Value<*> -> valueState.value as T
                is ValueState.Lazy<*> -> valueState.thunk(lazyContext) as T
            }
        } else {
            if (parent != null) {
                return parent.getOrNull(item, lazyContext)
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

    override fun <T : Any> getOrNull(name: String): T? {
        val item = getItemOrNull(name) ?: return null
        @Suppress("UNCHECKED_CAST")
        return get(item as Item<T>)
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

    override fun <T : Any> set(item: Item<T>, value: T) {
        if (value::class.isSubclassOf(item.type)) {
            if (item in this) {
                lock.write {
                    val valueState = valueByItem[item]!!
                    if (valueState is ValueState.Value<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (valueState as ValueState.Value<T>).value = value
                    } else {
                        valueByItem[item] = ValueState.Value(value)
                    }
                }
            } else {
                throw NoSuchElementException("cannot find ${item.name} in config")
            }
        } else {
            throw ClassCastException(
                    "fail to cast $value with ${value::class} to ${item.type}" +
                            " when setting ${item.name} in config")
        }
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

    override fun addSpec(spec: ConfigSpec) {
        lock.write {
            spec.items.forEach { item ->
                val name = item.name
                if (item !in this) {
                    if (name !in this) {
                        nameByItem[item] = name
                        valueByItem[item] = when (item) {
                            is OptionalItem -> ValueState.Value(item.default)
                            is RequiredItem -> ValueState.Unset
                            is LazyItem -> ValueState.Lazy(item.thunk)
                        }
                    } else {
                        throw RepeatedNameException("item $name has been added")
                    }
                } else {
                    throw RepeatedItemException("item $name has been added")
                }
            }
        }
    }

    override fun withLayer(name: String) = ConfigImpl(name, this)

    private sealed class ValueState {
        object Unset : ValueState()
        data class Lazy<T>(var thunk: (ConfigGetter) -> T) : ValueState()
        data class Value<T>(var value: T) : ValueState()
    }
}
