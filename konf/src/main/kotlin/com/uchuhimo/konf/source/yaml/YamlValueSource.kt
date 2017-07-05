package com.uchuhimo.konf.source.yaml

import com.uchuhimo.konf.Path
import com.uchuhimo.konf.source.NoSuchPathException
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.WrongTypeException
import com.uchuhimo.konf.unsupported
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

open class YamlValueSource(val value: Any) : Source {
    override val description: String get() = value.toString()

    override fun contains(path: Path): Boolean = false

    override fun getOrNull(path: Path): Source? {
        throw NoSuchPathException(this, path)
    }

    private inline fun <reified T> cast(): T {
        if (T::class.java.isInstance(value)) {
            return value as T
        } else {
            throw WrongTypeException(this, value::class.java.simpleName, T::class.java.simpleName)
        }
    }

    override fun toList(): List<Source> = cast<List<Any>>().map { it.asYamlSource() }

    override fun toMap(): Map<String, Source> = unsupported()

    override fun toText(): String = cast()

    override fun toBoolean(): Boolean = cast()

    override fun toLong(): Long {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return toInt().toLong()
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

    override fun toBigInteger(): BigInteger {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return BigInteger.valueOf(toLong())
        }
    }

    override fun toBigDecimal(): BigDecimal = BigDecimal.valueOf(toDouble())

    override fun toLocalDate(): LocalDate {
        try {
            return LocalDateTime.ofInstant(cast<Date>().toInstant(), ZoneOffset.UTC).toLocalDate()
        } catch (e: WrongTypeException) {
            return super.toLocalDate()
        }
    }

    override fun toLocalDateTime(): LocalDateTime {
        try {
            return LocalDateTime.ofInstant(cast<Date>().toInstant(), ZoneOffset.UTC)
        } catch (e: WrongTypeException) {
            return super.toLocalDateTime()
        }
    }

    override fun toDate(): Date {
        try {
            return cast()
        } catch (e: WrongTypeException) {
            return super.toDate()
        }
    }

    override fun toInstant(): Instant {
        try {
            return cast<Date>().toInstant()
        } catch (e: WrongTypeException) {
            return super.toInstant()
        }
    }
}