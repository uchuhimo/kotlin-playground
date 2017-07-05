package com.uchuhimo.konf.source.toml

import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.base.MapSource

class TomlMapSource(map: Map<String, Any>) : MapSource(map) {
    override fun Any.castToSource(): Source = asTomlSource()
}