package com.uchuhimo.konf.source.base

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.name
import com.uchuhimo.konf.source.NoSuchPathException
import com.uchuhimo.konf.source.ParseException
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.toPath

open class FlatMapSource(val map: Map<String, String>, val prefix: String = "") : Source {
    override val description: String
        get() = "flat map"

    override fun contains(path: Path): Boolean {
        if (path.isEmpty()) {
            return true
        } else {
            val fullPath = if (prefix.isEmpty()) path.name else "$prefix.${path.name}"
            return map.any { (key, _) ->
                if (key.startsWith(fullPath)) {
                    if (key == fullPath) {
                        true
                    } else {
                        val suffix = key.removePrefix(fullPath)
                        suffix.startsWith(".") && suffix.length > 1
                    }
                } else {
                    false
                }
            }
        }
    }

    override fun getOrNull(path: Path): Source? {
        if (path.isEmpty()) {
            return this
        } else {
            if (contains(path)) {
                if (prefix.isEmpty()) {
                    return FlatMapSource(map, path.name)
                } else {
                    return FlatMapSource(map, "$prefix.${path.name}")
                }
            } else {
                return null
            }
        }
    }

    private fun getValue(): String =
            map[prefix] ?: throw NoSuchPathException(this, prefix.toPath())

    override fun toList(): List<Source> {
        return generateSequence(0) { it + 1 }.map {
            getOrNull(it.toString().toPath())
        }.takeWhile {
            it != null
        }.filterNotNull().toList()
    }

    override fun toMap(): Map<String, Source> {
        return map.keys.filter {
            it.startsWith("$prefix.")
        }.map {
            it.removePrefix("$prefix.")
        }.filter {
            it.isNotEmpty()
        }.map {
            it.takeWhile { it != '.' }
        }.toSet().associate {
            it to FlatMapSource(map, "$prefix.$it")
        }
    }

    override fun toText(): String = getValue()

    override fun toBoolean(): Boolean {
        val value = getValue()
        try {
            return value.toBoolean()
        } catch (cause: NumberFormatException) {
            throw ParseException("$value cannot be parsed to a boolean", cause)
        }
    }

    override fun toDouble(): Double {
        val value = getValue()
        try {
            return value.toDouble()
        } catch (cause: NumberFormatException) {
            throw ParseException("$value cannot be parsed to a double", cause)
        }
    }

    override fun toInt(): Int {
        val value = getValue()
        try {
            return value.toInt()
        } catch (cause: NumberFormatException) {
            throw ParseException("$value cannot be parsed to an int", cause)
        }
    }

    override fun toLong(): Long {
        val value = getValue()
        try {
            return value.toLong()
        } catch (cause: NumberFormatException) {
            throw ParseException("$value cannot be parsed to a long", cause)
        }
    }
}