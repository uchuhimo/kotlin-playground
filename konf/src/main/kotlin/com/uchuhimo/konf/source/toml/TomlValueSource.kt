package com.uchuhimo.konf.source.toml

import com.uchuhimo.konf.source.ParseException
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.base.ValueSource

class TomlValueSource(value: Any) : ValueSource(value) {
    override fun Any.castToSource(): Source = asTomlSource()

    override fun toLong(): Long = cast()

    override fun toInt(): Int = toLong().also { value ->
        if (value < Int.MIN_VALUE || value > Int.MAX_VALUE) {
            throw ParseException("$value is out of range of Int")
        }
    }.toInt()
}

fun Any.asTomlSource(): Source =
        if (this is Source) {
            this
        } else if (this is Map<*, *>) {
            try {
                TomlMapSource(this as Map<String, Any>)
            } catch (e: ClassCastException) {
                TomlValueSource(this)
            }
        } else {
            TomlValueSource(this)
        }