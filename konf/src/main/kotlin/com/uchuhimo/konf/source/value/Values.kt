package com.uchuhimo.konf.source.value

import com.uchuhimo.konf.source.Source

fun Any.asSource(): Source =
        if (Map::class.java.isInstance(this)) {
            try {
                MapSource(this as Map<String, Any>)
            } catch (e: ClassCastException) {
                ValueSource(this)
            }
        } else {
            ValueSource(this)
        }