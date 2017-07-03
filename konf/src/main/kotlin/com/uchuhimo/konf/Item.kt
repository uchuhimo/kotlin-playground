package com.uchuhimo.konf

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory

sealed class Item<T : Any>(
        val spec: ConfigSpec,
        name: String,
        val description: String = "") {
    init {
        spec.addItem(this)
    }

    val name: String = spec.qualify(name)

    val path: List<String> = run {
        val path = this.name.split('.')
        check("" !in path) { "${this.name} is invalid name for item" }
        path
    }

    val type: JavaType = TypeFactory.defaultInstance().constructType(this::class.java)
            .findSuperType(Item::class.java).bindings.typeParameters[0]
}

open class RequiredItem<T : Any>(
        spec: ConfigSpec,
        name: String,
        description: String = ""
) : Item<T>(spec, name, description)

open class OptionalItem<T : Any>(
        spec: ConfigSpec,
        name: String,
        val default: T,
        description: String = ""
) : Item<T>(spec, name, description)

open class LazyItem<T : Any>(
        spec: ConfigSpec,
        name: String,
        val thunk: (ConfigGetter) -> T,
        val placeholder: String = "",
        description: String = ""
) : Item<T>(spec, name, description)