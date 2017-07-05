package com.uchuhimo.konf.source.yaml

import com.uchuhimo.konf.source.Source

fun Any.asYamlSource(): Source =
        if (Map::class.java.isInstance(this)) {
            try {
                YamlMapSource(this as Map<String, Any>)
            } catch (e: ClassCastException) {
                YamlValueSource(this)
            }
        } else {
            YamlValueSource(this)
        }