package com.uchuhimo.konf.source.base

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.name
import com.uchuhimo.konf.source.Source

open class KVSource(val map: Map<String, Any>) : ValueSource(map) {
    override val description: String get() = map.toString()

    override fun contains(path: Path): Boolean = map.contains(path.name)

    override fun getOrNull(path: Path): Source? = map[path.name]?.castToSource()

    override fun toMap(): Map<String, Source> = map.mapValues { (_, value) -> value.castToSource() }
}

fun Map<String, Any>.asKVSource() = KVSource(this)