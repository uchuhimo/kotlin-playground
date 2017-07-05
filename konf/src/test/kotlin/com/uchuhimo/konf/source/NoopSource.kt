package com.uchuhimo.konf.source

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.unsupported

open class NoopSource : Source {
    override val description: String get() = "noop"

    override fun contains(path: Path): Boolean = unsupported()

    override fun getOrNull(path: Path): Source? = unsupported()

    override fun toList(): List<Source> = unsupported()

    override fun toMap(): Map<String, Source> = unsupported()

    override fun toText(): String = unsupported()

    override fun toBoolean(): Boolean = unsupported()

    override fun toLong(): Long = unsupported()

    override fun toDouble(): Double = unsupported()
}