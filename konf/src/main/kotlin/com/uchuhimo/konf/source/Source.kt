package com.uchuhimo.konf.source

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BigIntegerNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.FloatNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ShortNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.node.TreeTraversingParser
import com.fasterxml.jackson.databind.type.ArrayType
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.MapLikeType
import com.fasterxml.jackson.databind.type.SimpleType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.typesafe.config.impl.ConfigImplUtil
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Path
import com.uchuhimo.konf.SizeInBytes
import com.uchuhimo.konf.getUnits
import com.uchuhimo.konf.source.deserializer.OffsetDateTimeDeserializer
import com.uchuhimo.konf.source.deserializer.DurationDeserializer
import com.uchuhimo.konf.source.deserializer.ZoneDateTimeDeserializer
import com.uchuhimo.konf.source.json.JsonSource
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
import java.time.ZoneOffset
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
    val description: String get() = (info + context).toDescription()

    val context: Map<String, String>

    fun addContext(name: String, value: String): Unit

    val info: Map<String, String>

    fun addInfo(name: String, value: String): Unit

    fun contains(path: Path): Boolean

    fun getOrNull(path: Path): Source?

    fun get(path: Path): Source = getOrNull(path) ?: throw NoSuchPathException(this, path)

    fun contains(key: String): Boolean = contains(key.toPath())

    fun getOrNull(key: String): Source? = getOrNull(key.toPath())

    fun get(key: String): Source = get(key.toPath())

    fun isList(): Boolean = false

    fun toList(): List<Source>

    fun isMap(): Boolean = false

    fun toMap(): Map<String, Source>

    fun isText(): Boolean = false

    fun toText(): String

    fun isBoolean(): Boolean = false

    fun toBoolean(): Boolean

    fun isLong(): Boolean = false

    fun toLong(): Long = toInt().toLong()

    fun isDouble(): Boolean = false

    fun toDouble(): Double

    fun isInt(): Boolean = false

    fun toInt(): Int

    fun isShort(): Boolean = false

    fun toShort(): Short = toInt().also { value ->
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw ParseException("$value is out of range of Short")
        }
    }.toShort()

    fun isByte(): Boolean = false

    fun toByte(): Byte = toInt().also { value ->
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw ParseException("$value is out of range of Byte")
        }
    }.toByte()

    fun isFloat(): Boolean = false

    fun toFloat(): Float = toDouble().toFloat()

    fun isChar(): Boolean = false

    fun toChar(): Char {
        val value = toText()
        if (value.length != 1) {
            throw WrongTypeException(this, "String", "Char")
        }
        return value[0]
    }

    fun isBigInteger(): Boolean = false

    fun toBigInteger(): BigInteger = BigInteger.valueOf(toLong())

    fun isBigDecimal(): Boolean = false

    fun toBigDecimal(): BigDecimal = BigDecimal.valueOf(toDouble())

    private inline fun <T> tryParse(block: () -> T): T {
        try {
            return block()
        } catch (cause: DateTimeParseException) {
            throw ParseException("fail to parse \"${toText()}\" as data time", cause)
        }
    }

    fun isOffsetTime(): Boolean = false

    fun toOffsetTime(): OffsetTime = tryParse { OffsetTime.parse(toText()) }

    fun isOffsetDateTime(): Boolean = false

    fun toOffsetDateTime(): OffsetDateTime = tryParse { OffsetDateTime.parse(toText()) }

    fun isZonedDateTime(): Boolean = false

    fun toZonedDateTime(): ZonedDateTime = tryParse { ZonedDateTime.parse(toText()) }

    fun isLocalDate(): Boolean = false

    fun toLocalDate(): LocalDate = tryParse { LocalDate.parse(toText()) }

    fun isLocalTime(): Boolean = false

    fun toLocalTime(): LocalTime = tryParse { LocalTime.parse(toText()) }

    fun isLocalDateTime(): Boolean = false

    fun toLocalDateTime(): LocalDateTime = tryParse { LocalDateTime.parse(toText()) }

    fun isDate(): Boolean = false

    fun toDate(): Date {
        try {
            return Date.from(tryParse { Instant.parse(toText()) })
        } catch (e: ParseException) {
            try {
                return Date.from(tryParse {
                    LocalDateTime.parse(toText())
                }.toInstant(ZoneOffset.UTC))
            } catch (e: ParseException) {
                return Date.from(tryParse {
                    LocalDate.parse(toText())
                }.atStartOfDay().toInstant(ZoneOffset.UTC))
            }
        }
    }

    fun isYear(): Boolean = false

    fun toYear(): Year = tryParse { Year.parse(toText()) }

    fun isYearMonth(): Boolean = false

    fun toYearMonth(): YearMonth = tryParse { YearMonth.parse(toText()) }

    fun isInstant(): Boolean = false

    fun toInstant(): Instant = tryParse { Instant.parse(toText()) }

    fun isDuration(): Boolean = false

    fun toDuration(): Duration = toText().toDuration()

    fun isSizeInBytes(): Boolean = false

    fun toSizeInBytes(): SizeInBytes = SizeInBytes.parse(toText())
}

fun Map<String, String>.toDescription() = map { (name, value) ->
    "$name: $value"
}.joinToString(separator = ", ", prefix = "[", postfix = "]")

fun String.toPath(): Path = listOf(this)

fun Source.withFallback(fallback: Source): Source = object : Source by this {
    init {
        addInfo("fallback", fallback.description)
    }

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

internal fun Config.loadFromSource(source: Source): Config {
    return withLayer("source: ${source.description}").apply {
        for (item in this) {
            val path = item.path
            if (source.contains(path)) {
                try {
                    rawSet(item, source.get(path).toValue(item.type, mapper))
                } catch (cause: SourceException) {
                    throw LoadException(path, cause)
                }
            }
        }
    }
}

private fun Source.toValue(type: JavaType, mapper: ObjectMapper): Any {
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
                    Date::class.java -> toDate()
                    Year::class.java -> toYear()
                    YearMonth::class.java -> toYearMonth()
                    Instant::class.java -> toInstant()
                    Duration::class.java -> toDuration()
                    SizeInBytes::class.java -> toSizeInBytes()
                    else -> {
                        try {
                            mapper.readValue<Any>(
                                    TreeTraversingParser(toJsonNode(), mapper),
                                    type)
                        } catch (cause: JsonProcessingException) {
                            throw UnsupportedTypeException(this, clazz, cause)
                        }
                    }
                }
            }
        }
        is ArrayType -> {
            val clazz = type.contentType.rawClass
            val list = toListValue(type.contentType, mapper)
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
                    addAll(toListValue(type.contentType, mapper))
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
                            value.toValue(type.contentType, mapper)
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

fun createDefaultMapper(): ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule()
                .addDeserializer(Duration::class.java, DurationDeserializer)
                .addDeserializer(OffsetDateTime::class.java, OffsetDateTimeDeserializer)
                .addDeserializer(ZonedDateTime::class.java, ZoneDateTimeDeserializer))

private fun Source.toListValue(type: JavaType, mapper: ObjectMapper) =
        toList().map { it.toValue(type, mapper) }

private fun Source.toJsonNode(): JsonNode {
    if (this is JsonSource) {
        return this.node
    } else {
        return when {
            isBoolean() -> BooleanNode.valueOf(toBoolean())
            isLong() -> LongNode.valueOf(toLong())
            isInt() -> IntNode.valueOf(toInt())
            isShort() -> ShortNode.valueOf(toShort())
            isByte() -> ShortNode.valueOf(toByte().toShort())
            isBigInteger() -> BigIntegerNode.valueOf(toBigInteger())
            isDouble() -> DoubleNode.valueOf(toDouble())
            isFloat() -> FloatNode.valueOf(toFloat())
            isChar() -> TextNode.valueOf(toChar().toString())
            isBigDecimal() -> DecimalNode.valueOf(toBigDecimal())
            isText() -> TextNode.valueOf(toText())
            isOffsetTime() -> TextNode.valueOf(toOffsetTime().toString())
            isOffsetDateTime() -> TextNode.valueOf(toOffsetDateTime().toString())
            isZonedDateTime() -> TextNode.valueOf(toZonedDateTime().toString())
            isLocalDate() -> TextNode.valueOf(toLocalDate().toString())
            isLocalTime() -> TextNode.valueOf(toLocalTime().toString())
            isLocalDateTime() -> TextNode.valueOf(toLocalDateTime().toString())
            isDate() -> TextNode.valueOf(toDate().toInstant().toString())
            isYear() -> TextNode.valueOf(toYear().toString())
            isYearMonth() -> TextNode.valueOf(toYearMonth().toString())
            isInstant() -> TextNode.valueOf(toInstant().toString())
            isDuration() -> TextNode.valueOf(toDuration().toString())
            isSizeInBytes() -> LongNode.valueOf(toSizeInBytes().bytes)
            isList() -> ArrayNode(
                    JsonNodeFactory.instance,
                    toList().map {
                        it.toJsonNode()
                    })
            isMap() -> ObjectNode(
                    JsonNodeFactory.instance,
                    toMap().mapValues { (_, value) ->
                        value.toJsonNode()
                    }
            )
            else -> throw ParseException("fail to cast source $description to JSON node")
        }
    }
}

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
