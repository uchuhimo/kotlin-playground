package com.uchuhimo.konf

import kotlin.reflect.KClass

open class ConfigSpec(val prefix: String) {
    private val _items = mutableListOf<Item<*>>()

    val items: List<Item<*>> = _items

    private fun qualify(name: String) = "$prefix.$name"

    inline fun <reified T : Any> required(name: String, description: String = "") =
            required(T::class, name, description)

    fun <T : Any> required(type: KClass<T>, name: String, description: String = "") =
            RequiredItem(
                    name = qualify(name),
                    type = type,
                    description = description
            ).also { addItem(it) }

    inline fun <reified T : Any> optional(name: String, default: T, description: String = "") =
            optional(T::class, name, default, description)

    fun <T : Any> optional(type: KClass<T>, name: String, default: T, description: String = "") =
            OptionalItem(
                    name = qualify(name),
                    type = type,
                    default = default,
                    description = description
            ).also { addItem(it) }

    inline fun <reified T : Any> lazy(
            name: String,
            description: String = "",
            placeholder: String = "",
            noinline default: (ConfigGetter) -> T) =
            lazy(T::class, name, description, placeholder, default)

    fun <T : Any> lazy(
            type: KClass<T>,
            name: String,
            description: String = "",
            placeholder: String = "",
            default: (ConfigGetter) -> T) =
            LazyItem(
                    name = qualify(name),
                    type = type,
                    thunk = default,
                    placeholder = placeholder,
                    description = description
            ).also { addItem(it) }

    fun addItem(item: Item<*>) {
        _items += item
    }
}