package com.uchuhimo.konf.source.yaml

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.source.NoSuchPathException
import com.uchuhimo.konf.source.Source

class YamlMapSource(val map: Map<String, Any>) : YamlValueSource(map) {
    override val description: String get() = map.toString()

    override fun contains(path: Path): Boolean {
        if (path.isEmpty()) {
            throw NoSuchPathException(this, path)
        } else {
            val key = path.first()
            val rest = path.drop(1)
            if (rest.isEmpty()) {
                return map.contains(key)
            } else {
                return map[key]?.asYamlSource()?.contains(rest) ?: false
            }
        }
    }

    override fun getOrNull(path: Path): Source? {
        if (path.isEmpty()) {
            throw NoSuchPathException(this, path)
        } else {
            val key = path.first()
            val rest = path.drop(1)
            if (rest.isEmpty()) {
                return map[key]?.asYamlSource()
            } else {
                return map[key]?.asYamlSource()?.getOrNull(rest)
            }
        }
    }

    override fun toMap(): Map<String, Source> = map.mapValues { (_, value) -> value.asYamlSource() }
}
