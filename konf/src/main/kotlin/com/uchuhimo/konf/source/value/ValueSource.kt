package com.uchuhimo.konf.source.value

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.SizeInBytes
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.WrongTypeException
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
import java.time.ZonedDateTime

open class ValueSource(val value: Any) : Source {
    override val description: String get() = value.toString()

    override fun contains(path: Path): Boolean = unsupported()

    override fun getOrNull(path: Path): Source? = unsupported()

    private inline fun <reified T> cast(): T {
        if (T::class.java.isInstance(value)) {
            return value as T
        } else {
            throw WrongTypeException(this, value::class.java.simpleName, T::class.java.simpleName)
        }
    }

    override fun toList(): List<Source> = cast<List<Any>>().map { it.asSource() }

    override fun toMap(): Map<String, Source> = unsupported()

    override fun toText(): String = cast()

    override fun toBoolean(): Boolean = cast()

    override fun toLong(): Long = cast()

    override fun toDouble(): Double = cast()

    override fun toInt(): Int = cast()

    override fun toShort(): Short = cast()

    override fun toByte(): Byte = cast()

    override fun toFloat(): Float = cast()

    override fun toChar(): Char = cast()

    override fun toBigInteger(): BigInteger = cast()

    override fun toBigDecimal(): BigDecimal = cast()

    override fun toOffsetTime(): OffsetTime = cast()

    override fun toOffsetDateTime(): OffsetDateTime = cast()

    override fun toZonedDateTime(): ZonedDateTime = cast()

    override fun toLocalDate(): LocalDate = cast()

    override fun toLocalTime(): LocalTime = cast()

    override fun toLocalDateTime(): LocalDateTime = cast()

    override fun toYear(): Year = cast()

    override fun toYearMonth(): YearMonth = cast()

    override fun toInstant(): Instant = cast()

    override fun toDuration(): Duration = cast()

    override fun toSizeInBytes(): SizeInBytes = cast()
}