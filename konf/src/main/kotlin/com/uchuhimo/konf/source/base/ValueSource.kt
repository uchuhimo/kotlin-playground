package com.uchuhimo.konf.source.base

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.SizeInBytes
import com.uchuhimo.konf.notEmptyOr
import com.uchuhimo.konf.source.NoSuchPathException
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.WrongTypeException
import com.uchuhimo.konf.source.toDescription
import com.uchuhimo.konf.unsupported
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

open class ValueSource(
        val value: Any,
        type: String = "",
        context: Map<String, String> = mapOf()
) : Source {
    val _info = mutableMapOf(
            "type" to type.notEmptyOr("value"))

    override val info: Map<String, String> get() = _info

    override fun addInfo(name: String, value: String) {
        _info.put(name, value)
    }

    val _context: MutableMap<String, String> = context.toMutableMap()

    override val context: Map<String, String> get() = _context

    override fun addContext(name: String, value: String) {
        _context.put(name, value)
    }

    override fun contains(path: Path): Boolean = path.isEmpty()

    override fun getOrNull(path: Path): Source? {
        if (path.isEmpty()) {
            return this
        } else {
            throw NoSuchPathException(this, path)
        }
    }

    protected inline fun <reified T> cast(): T {
        if (T::class.java.isInstance(value)) {
            return value as T
        } else {
            throw WrongTypeException(this, value::class.java.simpleName, T::class.java.simpleName)
        }
    }

    open fun Any.castToSource(context: Map<String, String>): Source = asSource(context = context)

    override fun toList(): List<Source> = cast<List<Any>>().map {
        it.castToSource(context).apply { addInfo("inList", this@ValueSource.info.toDescription()) }
    }

    override fun toMap(): Map<String, Source> = unsupported()

    override fun toText(): String = cast()

    override fun toBoolean(): Boolean = cast()

    override fun toLong(): Long {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toLong()
        }
    }

    override fun toDouble(): Double {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return toLong().toDouble()
        }
    }

    override fun toInt(): Int = cast()

    override fun toShort(): Short {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toShort()
        }
    }

    override fun toByte(): Byte {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toByte()
        }
    }

    override fun toFloat(): Float {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toFloat()
        }
    }

    override fun toChar(): Char {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toChar()
        }
    }

    override fun toBigInteger(): BigInteger {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toBigInteger()
        }
    }

    override fun toBigDecimal(): BigDecimal {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toBigDecimal()
        }
    }

    override fun toOffsetTime(): OffsetTime {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toOffsetTime()
        }
    }

    override fun toOffsetDateTime(): OffsetDateTime {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toOffsetDateTime()
        }
    }

    override fun toZonedDateTime(): ZonedDateTime {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toZonedDateTime()
        }
    }

    override fun toLocalDate(): LocalDate {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            try {
                return LocalDateTime.ofInstant(cast<Date>().toInstant(), ZoneOffset.UTC).toLocalDate()
            } catch (e: WrongTypeException) {
                return super.toLocalDate()
            }
        }
    }

    override fun toLocalTime(): LocalTime {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toLocalTime()
        }
    }

    override fun toLocalDateTime(): LocalDateTime {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            try {
                return LocalDateTime.ofInstant(cast<Date>().toInstant(), ZoneOffset.UTC)
            } catch (e: WrongTypeException) {
                return super.toLocalDateTime()
            }
        }
    }

    override fun toDate(): Date {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toDate()
        }
    }

    override fun toYear(): Year {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toYear()
        }
    }

    override fun toYearMonth(): YearMonth {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toYearMonth()
        }
    }

    override fun toInstant(): Instant {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            try {
                return cast<Date>().toInstant()
            } catch (e: WrongTypeException) {
                return super.toInstant()
            }
        }
    }

    override fun toDuration(): Duration {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toDuration()
        }
    }

    override fun toSizeInBytes(): SizeInBytes {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toSizeInBytes()
        }
    }
}

fun Any.asSource(type: String = "", context: Map<String, String> = mapOf()): Source =
        if (this is Source) {
            this
        } else if (this is Map<*, *>) {
            try {
                MapSource(this as Map<String, Any>, type, context)
            } catch (e: ClassCastException) {
                ValueSource(this, type, context)
            }
        } else {
            ValueSource(this, type, context)
        }