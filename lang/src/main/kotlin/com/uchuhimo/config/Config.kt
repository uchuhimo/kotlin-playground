package com.uchuhimo.config

import com.uchuhimo.collections.mutableBiMapOf
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

sealed class Item<T : Any>(
        val name: String,
        val type: KClass<T>,
        val description: String = "")

class RequiredItem<T : Any>(name: String, type: KClass<T>, description: String = "") :
        Item<T>(name, type, description)

class OptionalItem<T : Any>(
        name: String,
        type: KClass<T>,
        val default: T,
        description: String = ""
) : Item<T>(name, type, description)

class LazyItem<T : Any>(
        name: String,
        type: KClass<T>,
        val thunk: (ConfigGetter) -> T,
        description: String = ""
) : Item<T>(name, type, description)

interface ConfigGetter {
    operator fun <T : Any> get(item: Item<T>): T
    operator fun <T : Any> get(name: String): T
    operator fun <T : Any> invoke(name: String): T = get(name)
}

interface Config : ConfigGetter {
    operator fun <T : Any> set(item: Item<T>, value: T)
    operator fun <T : Any> set(name: String, value: T)

    fun addSpec(spec: ConfigSpec)

    companion object {
        operator fun invoke(): Config = ConfigImpl()
    }
}

class RepeatedItemException(message: String) : Exception(message)

class RepeatedNameException(message: String) : Exception(message)

private class ConfigImpl : Config {
    private val valueByItem = mutableMapOf<Item<*>, ValueState>()
    private val nameByItem = mutableBiMapOf<Item<*>, String>()

    private val lock = ReentrantReadWriteLock()

    override fun <T : Any> get(item: Item<T>): T {
        val valueState = lock.read { valueByItem[item] } ?:
                throw NoSuchElementException("cannot find ${item.name} in config")
        @Suppress("UNCHECKED_CAST")
        return when (valueState) {
            is ValueState.Unset -> error("${item.name} is unset")
            is ValueState.Value<*> -> valueState.value as T
            is ValueState.Lazy<*> -> valueState.thunk(this) as T
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
        data class Lazy<T>(val thunk: (Config) -> T) : ValueState()
        data class Value<T>(val value: T) : ValueState()
    }
}

open class ConfigSpec(val prefix: String) {
    private val _items = mutableListOf<Item<*>>()

    val items: List<Item<*>> = _items

    private fun qualify(name: String) = "$prefix.$name"

    inline fun <reified T : Any> required(name: String, description: String = "") =
            required(T::class, name, description)

    fun <T : Any> required(type: KClass<T>, name: String, description: String = "") =
            RequiredItem(qualify(name), type, description).also { addItem(it) }

    inline fun <reified T : Any> optional(name: String, default: T, description: String = "") =
            optional(T::class, name, default, description)

    fun <T : Any> optional(type: KClass<T>, name: String, default: T, description: String = "") =
            OptionalItem(qualify(name), type, default, description).also { addItem(it) }

    inline fun <reified T : Any> lazy(
            name: String,
            description: String = "",
            noinline default: (ConfigGetter) -> T) =
            lazy(T::class, name, description, default)

    fun <T : Any> lazy(
            type: KClass<T>,
            name: String,
            description: String = "",
            default: (ConfigGetter) -> T) =
            LazyItem(qualify(name), type, default, description).also { addItem(it) }

    fun addItem(item: Item<*>) {
        _items += item
    }
}

class Buffer {
    companion object : ConfigSpec("network.buffer") {
        val size = required<Int>(name = "size", description = "size of buffer in KB")
        val totalSize = lazy(
                name = "totalSize",
                description = "total size of buffer in KB") { it[size] * 2 }
        val name = optional(
                name = "name",
                default = "buffer",
                description = "name of buffer")
        val type = optional(
                name = "type",
                default = Type.OFF_HEAP,
                description = """
                              | position of network buffer.
                              | two type:
                              | - on-heap
                              | - off-heap
                              | buffer is off-heap by default.
                              """.trimMargin("| "))
    }

    enum class Type {
        ON_HEAP, OFF_HEAP
    }
}

fun main(args: Array<String>) {
    Buffer.items.forEach { println(it.name); println(it.description) }
    val config = Config().apply { addSpec(Buffer) }
    config.apply {
        addSpec(object : ConfigSpec("network.buffer") {
            init {
                optional("name1", 1)
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