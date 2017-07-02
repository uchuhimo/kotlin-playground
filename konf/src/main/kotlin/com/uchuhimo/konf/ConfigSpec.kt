package com.uchuhimo.konf

open class ConfigSpec(val prefix: String) {
    private val _items = mutableListOf<Item<*>>()

    val items: List<Item<*>> = _items

    fun qualify(name: String) = "$prefix.$name"

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any> required(name: String, description: String = "") =
            object : RequiredItem<T>(
                    name = qualify(name),
                    description = description
            ) {}.also { addItem(it) }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any> optional(name: String, default: T, description: String = "") =
            object : OptionalItem<T>(
                    name = qualify(name),
                    default = default,
                    description = description
            ) {}.also { addItem(it) }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any> lazy(
            name: String,
            description: String = "",
            placeholder: String = "",
            noinline default: (ConfigGetter) -> T) =
            object : LazyItem<T>(
                    name = qualify(name),
                    thunk = default,
                    placeholder = placeholder,
                    description = description
            ) {}.also { addItem(it) }

    fun addItem(item: Item<*>) {
        _items += item
    }
}