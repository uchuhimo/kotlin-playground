package com.uchuhimo.konf.source.base

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.source.Source

open class MapSource(val map: Map<String, Any>) : ValueSource(map) {
    override val description: String get() = map.toString()

    override fun contains(path: Path): Boolean {
        if (path.isEmpty()) {
            return true
        } else {
            val key = path.first()
            val rest = path.drop(1)
            return map[key]?.castToSource()?.contains(rest) ?: false
        }
    }

    override fun getOrNull(path: Path): Source? {
        if (path.isEmpty()) {
            return this
        } else {
            val key = path.first()
            val rest = path.drop(1)
            return map[key]?.castToSource()?.getOrNull(rest)
        }
    }

    override fun toMap(): Map<String, Source> = map.mapValues { (_, value) -> value.castToSource() }
}
