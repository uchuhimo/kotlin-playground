package com.uchuhimo.konf.source

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.ArrayType
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.MapLikeType
import com.fasterxml.jackson.databind.type.SimpleType
import com.typesafe.config.impl.ConfigImplUtil
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Path
import com.uchuhimo.konf.SizeInBytes
import com.uchuhimo.konf.getUnits
import java.lang.Class
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
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Byte
import kotlin.Char
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Short
import kotlin.String
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.component1
import kotlin.collections.component2

interface Source {
    val description: String

    fun contains(path: Path): Boolean

    fun getOrNull(path: Path): Source?

    fun get(path: Path): Source = getOrNull(path) ?: throw NoSuchPathException(this, path)

    fun contains(key: String): Boolean = contains(key.toPath())

    fun getOrNull(key: String): Source? = getOrNull(key.toPath())

    fun get(key: String): Source = get(key.toPath())

    fun toList(): List<Source>

    fun toMap(): Map<String, Source>

    fun toText(): String

    fun toBoolean(): Boolean

    fun toLong(): Long

    fun toDouble(): Double

    fun toInt(): Int = toLong().also { value ->
        if (value < Int.MIN_VALUE || value > Int.MAX_VALUE) {
            throw ParseException("$value is out of range of Int")
        }
    }.toInt()

    fun toShort(): Short = toInt().also { value ->
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw ParseException("$value is out of range of Short")
        }
    }.toShort()

    fun toByte(): Byte = toInt().also { value ->
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw ParseException("$value is out of range of Byte")
        }
    }.toByte()

    fun toFloat(): Float = toDouble().toFloat()

    fun toChar(): Char {
        val value = toText()
        if (value.length != 1) {
            throw WrongTypeException(this, "String", "Char")
        }
        return value[0]
    }

    fun toBigInteger(): BigInteger = BigInteger.valueOf(toLong())

    fun toBigDecimal(): BigDecimal = BigDecimal.valueOf(toDouble())

    private inline fun <T> tryParse(block: () -> T): T {
        try {
            return block()
        } catch (cause: DateTimeParseException) {
            throw ParseException("fail to parse \"${toText()}\" as data time", cause)
        }
    }

    fun toOffsetTime(): OffsetTime = tryParse { OffsetTime.parse(toText()) }

    fun toOffsetDateTime(): OffsetDateTime = tryParse { OffsetDateTime.parse(toText()) }

    fun toZonedDateTime(): ZonedDateTime = tryParse { ZonedDateTime.parse(toText()) }

    fun toLocalDate(): LocalDate = tryParse { LocalDate.parse(toText()) }

    fun toLocalTime(): LocalTime = tryParse { LocalTime.parse(toText()) }

    fun toLocalDateTime(): LocalDateTime = tryParse { LocalDateTime.parse(toText()) }

    fun toYear(): Year = tryParse { Year.parse(toText()) }

    fun toYearMonth(): YearMonth = tryParse { YearMonth.parse(toText()) }

    fun toInstant(): Instant = tryParse { Instant.parse(toText()) }

    fun toDuration(): Duration = toText().toDuration()

    fun toSizeInBytes(): SizeInBytes = SizeInBytes.parse(toText())
}

fun String.toPath(): Path = listOf(this)

fun Source.withFallback(fallback: Source): Source = object : Source by this {
    override fun contains(path: List<String>): Boolean =
            this@withFallback.contains(path) || fallback.contains(path)

    override fun get(path: List<String>): Source =
            this@withFallback.getOrNull(path) ?: fallback.get(path)

    override fun getOrNull(path: List<String>): Source? =
            this@withFallback.getOrNull(path) ?: fallback.getOrNull(path)

    override fun contains(key: String): Boolean =
            this@withFallback.contains(key) || fallback.contains(key)

    override fun get(key: String): Source =
            this@withFallback.getOrNull(key) ?: fallback.get(key)

    override fun getOrNull(key: String): Source? =
            this@withFallback.getOrNull(key) ?: fallback.getOrNull(key)
}

fun Config.load(source: Source) {
    for (item in this) {
        val path = item.path
        if (source.contains(path)) {
            try {
                rawSet(item, source.get(path).toValue(item.type))
            } catch (cause: SourceException) {
                throw LoadException(path, cause)
            }
        }
    }
}

private fun Source.toValue(type: JavaType): Any {
    when (type) {
        is SimpleType -> {
            val clazz = type.rawClass
            if (type.isEnumType) {
                val valueOfMethod = clazz.getMethod("valueOf", String::class.java)
                val name = toText()
                try {
                    return valueOfMethod.invoke(null, name)
                } catch (cause: IllegalArgumentException) {
                    throw ParseException(
                            "enum type $clazz has no constant with name $name", cause)
                }
            } else {
                return when (clazz) {
                    Boolean::class.javaObjectType, Boolean::class.java -> toBoolean()
                    Int::class.javaObjectType, Int::class.java -> toInt()
                    Short::class.javaObjectType, Short::class.java -> toShort()
                    Byte::class.javaObjectType, Byte::class.java -> toByte()
                    Long::class.javaObjectType, Long::class.java -> toLong()
                    BigInteger::class.java -> toBigInteger()
                    Double::class.javaObjectType, Double::class.java -> toDouble()
                    Float::class.javaObjectType, Float::class.java -> toFloat()
                    BigDecimal::class.java -> toBigDecimal()
                    Char::class.javaObjectType, Char::class.java -> toChar()
                    String::class.java -> toText()
                    OffsetTime::class.java -> toOffsetTime()
                    OffsetDateTime::class.java -> toOffsetDateTime()
                    ZonedDateTime::class.java -> toZonedDateTime()
                    LocalDate::class.java -> toLocalDate()
                    LocalTime::class.java -> toLocalTime()
                    LocalDateTime::class.java -> toLocalDateTime()
                    Year::class.java -> toYear()
                    YearMonth::class.java -> toYearMonth()
                    Instant::class.java -> toInstant()
                    Duration::class.java -> toDuration()
                    SizeInBytes::class.java -> toSizeInBytes()
                    else -> throw UnsupportedTypeException(this, clazz)
                }
            }
        }
        is ArrayType -> {
            val clazz = type.contentType.rawClass
            val list = toListValue(type.contentType)
            if (!clazz.isPrimitive) {
                val array = java.lang.reflect.Array.newInstance(clazz, list.size) as Array<*>
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                return (list as java.util.Collection<*>).toArray(array)
            } else {
                @Suppress("UNCHECKED_CAST")
                return when (clazz) {
                    Boolean::class.java -> (list as List<Boolean>).toBooleanArray()
                    Int::class.java -> (list as List<Int>).toIntArray()
                    Short::class.java -> (list as List<Short>).toShortArray()
                    Byte::class.java -> (list as List<Byte>).toByteArray()
                    Long::class.java -> (list as List<Long>).toLongArray()
                    Double::class.java -> (list as List<Double>).toDoubleArray()
                    Float::class.java -> (list as List<Float>).toFloatArray()
                    Char::class.java -> (list as List<Char>).toCharArray()
                    else -> throw UnsupportedTypeException(this, clazz)
                }
            }
        }
        is CollectionLikeType -> {
            if (type.isTrueCollectionType) {
                @Suppress("UNCHECKED_CAST")
                return (implOf(type.rawClass).newInstance() as MutableCollection<Any>).apply {
                    addAll(toListValue(type.contentType))
                }
            } else {
                throw UnsupportedTypeException(this, type.rawClass)
            }
        }
        is MapLikeType -> {
            if (type.isTrueMapType) {
                if (type.keyType.rawClass == String::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    return (implOf(type.rawClass).newInstance() as MutableMap<String, Any>).apply {
                        putAll(this@toValue.toMap().mapValues { (_, value) ->
                            value.toValue(type.contentType)
                        })
                    }
                } else {
                    throw UnsupportedMapKeyException(type.keyType.rawClass)
                }
            } else {
                throw UnsupportedTypeException(this, type.rawClass)
            }
        }
        else -> throw UnsupportedTypeException(this, type.rawClass)
    }
}

private fun Source.toListValue(type: JavaType) = toList().map { it.toValue(type) }

private fun implOf(clazz: Class<*>): Class<*> =
        when (clazz) {
            List::class.java, MutableList::class.java -> ArrayList::class.java
            Set::class.java, MutableSet::class.java -> HashSet::class.java
            SortedSet::class.java -> TreeSet::class.java
            Map::class.java, MutableMap::class.java -> HashMap::class.java
            SortedMap::class.java -> TreeMap::class.java
            else -> clazz
        }

fun String.toDuration(): Duration {
    try {
        return Duration.parse(this)
    } catch (e: DateTimeParseException) {
        return Duration.ofNanos(parseDuration(this))
    }
}

/**
 * Parses a duration string. If no units are specified in the string, it is
 * assumed to be in milliseconds. The returned duration is in nanoseconds.
 *
 * @param input the string to parse
 *
 * @return duration in nanoseconds
 */
private fun parseDuration(input: String): Long {
    val s = ConfigImplUtil.unicodeTrim(input)
    val originalUnitString = getUnits(s)
    var unitString = originalUnitString
    val numberString = ConfigImplUtil.unicodeTrim(s.substring(0, s.length - unitString.length))
    val units: TimeUnit?

    // this would be caught later anyway, but the error message
    // is more helpful if we check it here.
    if (numberString.isEmpty())
        throw ParseException("No number in duration value '$input'")

    if (unitString.length > 2 && !unitString.endsWith("s"))
        unitString += "s"

    // note that this is deliberately case-sensitive
    if (unitString == "" || unitString == "ms" || unitString == "millis"
            || unitString == "milliseconds") {
        units = TimeUnit.MILLISECONDS
    } else if (unitString == "us" || unitString == "micros" || unitString == "microseconds") {
        units = TimeUnit.MICROSECONDS
    } else if (unitString == "ns" || unitString == "nanos" || unitString == "nanoseconds") {
        units = TimeUnit.NANOSECONDS
    } else if (unitString == "d" || unitString == "days") {
        units = TimeUnit.DAYS
    } else if (unitString == "h" || unitString == "hours") {
        units = TimeUnit.HOURS
    } else if (unitString == "s" || unitString == "seconds") {
        units = TimeUnit.SECONDS
    } else if (unitString == "m" || unitString == "minutes") {
        units = TimeUnit.MINUTES
    } else {
        throw ParseException("Could not parse time unit '$originalUnitString' (try ns, us, ms, s, m, h, d)")
    }

    try {
        // if the string is purely digits, parse as an integer to avoid
        // possible precision loss;
        // otherwise as a double.
        if (numberString.matches("[+-]?[0-9]+".toRegex())) {
            return units.toNanos(java.lang.Long.parseLong(numberString))
        } else {
            val nanosInUnit = units.toNanos(1)
            return (java.lang.Double.parseDouble(numberString) * nanosInUnit).toLong()
        }
    } catch (e: NumberFormatException) {
        throw ParseException("Could not parse duration number '$numberString'")
    }
}
