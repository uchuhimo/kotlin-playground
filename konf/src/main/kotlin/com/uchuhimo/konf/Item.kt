package com.uchuhimo.konf

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory

sealed class Item<T : Any>(
        val name: String,
        val description: String = "") {
    val path: List<String> = run {
        val path = name.split('.')
        check("" !in path) { "$name is invalid name for item" }
        path
    }
    val type: JavaType = TypeFactory.defaultInstance().constructType(this::class.java)
            .findSuperType(Item::class.java).bindings.typeParameters[0]
}

open class RequiredItem<T : Any>(name: String, description: String = "") :
        Item<T>(name, description)

open class OptionalItem<T : Any>(
        name: String,
        val default: T,
        description: String = ""
) : Item<T>(name, description)

open class LazyItem<T : Any>(
        name: String,
        val thunk: (ConfigGetter) -> T,
        val placeholder: String = "",
        description: String = ""
) : Item<T>(name, description)