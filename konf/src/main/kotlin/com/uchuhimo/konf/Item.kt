package com.uchuhimo.konf

import kotlin.reflect.KClass

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