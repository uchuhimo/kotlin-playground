package com.uchuhimo.config

import com.uchuhimo.collections.mutableBiMapOf
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

sealed class Item<T : Any> {
    abstract val name: String
    abstract val type: KClass<T>
}

class RequiredItem<T : Any>(
        override val name: String,
        override val type: KClass<T>
) : Item<T>()

data class OptionalItem<T : Any>(
        override val name: String,
        override val type: KClass<T>,
        val default: T
) : Item<T>()

data class LazyItem<T : Any>(
        override val name: String,
        override val type: KClass<T>,
        val default: (ConfigEnv) -> T
) : Item<T>()

interface ConfigEnv {
    operator fun <T : Any> get(item: Item<T>): T
    operator fun <T : Any> get(name: String): T
    operator fun <T : Any> invoke(name: String): T = get(name)
}

interface Config : ConfigEnv {
    operator fun <T : Any> set(item: Item<T>, value: T)
    operator fun <T : Any> set(name: String, value: T)

    fun addScope(scope: ConfigScope)

    companion object {
        operator fun invoke(): Config = ConfigImpl()
    }
}

class RepeatedItemException(message: String) : Exception(message)

class RepeatedNameException(message: String) : Exception(message)

class ConfigImpl : Config {
    private val valueByItem = mutableMapOf<Item<*>, ValueState<*>>()
    private val nameByItem = mutableBiMapOf<Item<*>, String>()

    private val lock = ReentrantReadWriteLock()

    override fun <T : Any> get(item: Item<T>): T {
        val valueState = lock.read { valueByItem[item] } ?:
                throw NoSuchElementException("cannot find ${item.name} in config")
        @Suppress("UNCHECKED_CAST")
        return when (valueState) {
            is ValueState.Unset -> error("${item.name} is unset")
            is ValueState.Value -> valueState.value as T
            is ValueState.Lazy -> valueState.thunk(this) as T
        }
    }

    override fun <T : Any> get(name: String): T {
        val item = lock.read { nameByItem.inverse[name] } ?:
                throw NoSuchElementException("cannot find $name in config")
        @Suppress("UNCHECKED_CAST")
        return get(item as Item<T>)
    }

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

    override fun addScope(scope: ConfigScope) {
        lock.write {
            scope.items.forEach { item ->
                val name = item.name
                if (!nameByItem.containsKey(item)) {
                    if (!nameByItem.containsValue(name)) {
                        nameByItem[item] = name
                        valueByItem[item] = when (item) {
                            is OptionalItem -> ValueState.Value(item.default)
                            is RequiredItem -> ValueState.Unset
                            is LazyItem -> ValueState.Lazy(item.default)
                        }
                    } else {
                        throw RepeatedNameException("item ${name} has been added")
                    }
                } else {
                    throw RepeatedItemException("item ${name} has been added")
                }
            }
        }
    }

    private sealed class ValueState<T> {
        object Unset : ValueState<Nothing>()
        data class Lazy<T>(val thunk: (Config) -> T) : ValueState<T>()
        data class Value<T>(val value: T) : ValueState<T>()
    }
}

open class ConfigScope(val prefix: String) {
    private val _items = mutableListOf<Item<*>>()

    val items: List<Item<*>> = _items

    private fun qualify(name: String) = "$prefix.$name"

    inline fun <reified T : Any> item(name: String) = item(name, T::class)

    fun <T : Any> item(name: String, type: KClass<T>) =
            RequiredItem(qualify(name), type).also { addItem(it) }

    inline fun <reified T : Any> item(name: String, default: T) = item(name, T::class, default)

    fun <T : Any> item(name: String, type: KClass<T>, default: T) =
            OptionalItem(qualify(name), type, default).also { addItem(it) }

    inline fun <reified T : Any> item(name: String, noinline default: (ConfigEnv) -> T) =
            item(name, T::class, default)

    fun <T : Any> item(name: String, type: KClass<T>, default: (ConfigEnv) -> T) =
            LazyItem(qualify(name), type, default).also { addItem(it) }

    fun addItem(item: Item<*>) {
        _items += item
    }
}

class Buffer {
    companion object : ConfigScope("network.buffer") {
        val size = item<Int>("size")
        val totalSize = item("totalSize") { it[size] * 2 }
        val name = item("name", "buffer")
        val type = item("type", Type.OFF_HEAP)
    }

    enum class Type {
        ON_HEAP, OFF_HEAP
    }
}

fun main(args: Array<String>) {
    Buffer.items.forEach { println(it) }
    val config = Config().apply { addScope(Buffer) }
    config.apply {
        addScope(object : ConfigScope("network.buffer") {
            init {
                item("name1", 1)
            }
        })
    }
    config[Buffer.size] = 1024
    config["network.buffer.type"] = Buffer.Type.ON_HEAP
    println(config[Buffer.totalSize])
    println(config[Buffer.name])
    println(config[Buffer.type])
    println(config.get<Buffer.Type>("network.buffer.type"))
    println(config<Buffer.Type>("network.buffer.type"))
    config[Buffer.size] = 2048
    println(config[Buffer.totalSize])
    config[Buffer.totalSize] = 0
    println(config[Buffer.totalSize])
    config[Buffer.size] = 1024
    println(config[Buffer.totalSize])
}