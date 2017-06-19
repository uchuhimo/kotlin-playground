package com.uchuhimo.konf

import com.uchuhimo.collections.mutableBiMapOf
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
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

    fun addSpec(spec: ConfigSpec)

    companion object {
        operator fun invoke(): Config = ConfigImpl()
        operator fun invoke(init: Config.() -> Unit): Config = Config().apply(init)
    }
}

class RepeatedItemException(message: String) : Exception(message)

class RepeatedNameException(message: String) : Exception(message)

private class ConfigImpl : Config {
    private val valueByItem = mutableMapOf<Item<*>, ValueState>()
    private val nameByItem = mutableBiMapOf<Item<*>, String>()

    private val lock = ReentrantReadWriteLock()

    override fun <T : Any> get(item: Item<T>): T = getOrNull(item) ?:
            throw NoSuchElementException("cannot find ${item.name} in config")

    override fun <T : Any> get(name: String): T = getOrNull<T>(name) ?:
            throw NoSuchElementException("cannot find $name in config")

    override fun <T : Any> getOrNull(item: Item<T>): T? {
        val valueState = lock.read { valueByItem[item] } ?: return null
        @Suppress("UNCHECKED_CAST")
        return when (valueState) {
            is ValueState.Unset -> error("${item.name} is unset")
            is ValueState.Value<*> -> valueState.value as T
            is ValueState.Lazy<*> -> valueState.thunk(this) as T
        }
    }

    override fun <T : Any> getOrNull(name: String): T? {
        val item = lock.read { nameByItem.inverse[name] } ?: return null
        @Suppress("UNCHECKED_CAST")
        return get(item as Item<T>)
    }

    override fun contains(item: Item<*>): Boolean = lock.read { valueByItem[item] } != null

    override fun contains(name: String): Boolean = lock.read { nameByItem.inverse[name] } != null

    override fun <T : Any> set(item: Item<T>, value: T) {
        if (value::class.isSubclassOf(item.type)) {
            lock.write {
                if (!valueByItem.contains(item)) {
                    throw NoSuchElementException("cannot find ${item.name} in config")
                }
                valueByItem[item] = ValueState.Value(value)
            }
        } else {
            throw ClassCastException(
                    "fail to cast $value with ${value::class} to ${item.type}" +
                            " when setting ${item.name} in config")
        }
    }

    override fun <T : Any> set(name: String, value: T) {
        val item = lock.read { nameByItem.inverse[name] } ?:
                throw NoSuchElementException("cannot find $name in config")
        @Suppress("UNCHECKED_CAST")
        set(item as Item<T>, value)
    }

    override fun addSpec(spec: ConfigSpec) {
        lock.write {
            spec.items.forEach { item ->
                val name = item.name
                if (!nameByItem.containsKey(item)) {
                    if (!nameByItem.containsValue(name)) {
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

    private sealed class ValueState {
        object Unset : ValueState()
        data class Lazy<out T>(val thunk: (Config) -> T) : ValueState()
        data class Value<out T>(val value: T) : ValueState()
    }
}
