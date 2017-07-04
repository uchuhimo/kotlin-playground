package com.uchuhimo.konf.source.value

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.name
import com.uchuhimo.konf.source.Source

class MapSource(val map: Map<String, Any>) : ValueSource(map) {
    override val description: String get() = map.toString()

    override fun contains(path: Path): Boolean = map.contains(path.name)

    override fun getOrNull(path: Path): Source? = map[path.name]?.asSource()

    override fun toMap(): Map<String, Source> = map.mapValues { (_, value) -> value.asSource() }
}