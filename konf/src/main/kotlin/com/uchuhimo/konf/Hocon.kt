package com.uchuhimo.konf

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.ArrayType
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.MapLikeType
import com.fasterxml.jackson.databind.type.SimpleType
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType
import com.typesafe.config.impl.ConfigImplUtil
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.SortedSet
import java.util.TreeSet
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import com.typesafe.config.Config as HoconConfig

private fun ConfigValue.checkType(type: ConfigValueType) {
    check(valueType() == type) { "expect type ${valueType()}, get type $type" }
}

private fun ConfigValue.toIntValue(): Int {
    checkType(ConfigValueType.NUMBER)
    val value = unwrapped()
    check(value is Int) { "expect an integer, get a ${value::class.java}" }
    return value as Int
}

private fun ConfigValue.toLongValue(): Long {
    checkType(ConfigValueType.NUMBER)
    val value = unwrapped()
    check(value is Int || value is Long) {
        "expect a long, get a ${value::class.java}"
    }
    if (value is Int) {
        return value.toLong()
    } else {
        return value as Long
    }
}

private fun ConfigValue.toStringValue(): String {
    checkType(ConfigValueType.STRING)
    return unwrapped() as String
}

private fun ConfigValue.toListValue(type: JavaType): List<Any> {
    checkType(ConfigValueType.LIST)
    return mutableListOf<Any>().apply {
        for (value in (this@toListValue as ConfigList)) {
            add(value.toItemValue(type))
        }
    }
}

private fun implOf(clazz: Class<*>): Class<*> =
        when (clazz) {
            List::class.java, MutableList::class.java -> ArrayList::class.java
            Set::class.java, MutableSet::class.java -> HashSet::class.java
            SortedSet::class.java -> TreeSet::class.java
            Map::class.java, MutableMap::class.java -> HashMap::class.java
            else -> clazz
        }

private fun ConfigValue.toItemValue(type: JavaType): Any {
    when (type) {
        is SimpleType -> {
            val clazz = type.rawClass
            when (clazz) {
                Boolean::class.javaObjectType, Boolean::class.java -> {
                    checkType(ConfigValueType.BOOLEAN)
                    return unwrapped() as Boolean
                }
                Int::class.javaObjectType, Int::class.java -> return toIntValue()
                Short::class.javaObjectType, Short::class.java -> return toIntValue().also { value ->
                    check(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                        "$value is out of range of short"
                    }
                }.toShort()
                Byte::class.javaObjectType, Byte::class.java -> return toIntValue().also { value ->
                    check(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                        "$value is out of range of byte"
                    }
                }.toByte()
                Long::class.javaObjectType, Long::class.java -> return toLongValue()
                BigInteger::class.java -> return BigInteger.valueOf(toLongValue())
                Double::class.javaObjectType, Double::class.java -> {
                    checkType(ConfigValueType.NUMBER)
                    return (unwrapped() as Number).toDouble()
                }
                Float::class.javaObjectType, Float::class.java -> {
                    checkType(ConfigValueType.NUMBER)
                    return (unwrapped() as Number).toFloat()
                }
                BigDecimal::class.java -> {
                    checkType(ConfigValueType.NUMBER)
                    return BigDecimal.valueOf((unwrapped() as Number).toDouble())
                }
                Char::class.javaObjectType, Char::class.java -> {
                    val value = toStringValue()
                    check(value.length == 1) { "fail to cast $value to Char" }
                    return value[0]
                }
                String::class.java -> return toStringValue()
                OffsetTime::class.java -> return OffsetTime.parse(toStringValue())
                OffsetDateTime::class.java -> return OffsetDateTime.parse(toStringValue())
                ZonedDateTime::class.java -> return ZonedDateTime.parse(toStringValue())
                LocalDate::class.java -> return LocalDate.parse(toStringValue())
                LocalTime::class.java -> return LocalTime.parse(toStringValue())
                LocalDateTime::class.java -> return LocalDateTime.parse(toStringValue())
                Instant::class.java -> return Instant.parse(toStringValue())
                Duration::class.java -> {
                    val value = toStringValue()
                    try {
                        return Duration.parse(value)
                    } catch (e: DateTimeParseException) {
                        return Duration.ofNanos(parseDuration(value))
                    }
                }
                SizeInBytes::class.java -> return SizeInBytes(parseBytes(toStringValue()))
                else -> {
                    if (type.isEnumType) {
                        val valueOfMethod = clazz.getMethod("valueOf", String::class.java)
                        val name = toStringValue()
                        try {
                            return valueOfMethod.invoke(null, name)
                        } catch (cause: IllegalArgumentException) {
                            throw IllegalStateException(
                                    "enum type $clazz has no constant with name $name", cause)
                        }
                    } else {
                        return unsupportedType(clazz)
                    }
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
                    else -> unsupportedType(clazz)
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
                return unsupportedType(type.rawClass)
            }
        }
        is MapLikeType -> {
            if (type.isTrueMapType) {
                if (type.keyType.rawClass == String::class.java) {
                    checkType(ConfigValueType.OBJECT)
                    @Suppress("UNCHECKED_CAST")
                    return (implOf(type.rawClass).newInstance() as MutableMap<String, Any>).apply {
                        for ((key, value) in (this@toItemValue as ConfigObject)) {
                            put(key, value.toItemValue(type.contentType))
                        }
                    }
                } else {
                    throw UnsupportedOperationException(
                            "cannot load map with ${type.keyType.rawClass} key, only support string key")
                }
            } else {
                return unsupportedType(type.rawClass)
            }
        }
        else -> return unsupportedType(type.rawClass)
    }
}

fun Config.loadHoconFile(file: File) {
    val config = ConfigFactory.parseFile(file)
    for (item in this) {
        val name = item.name
        if (config.hasPath(name)) {
            try {
                rawSet(item, config.getValue(name).toItemValue(item.type))
            } catch (cause: Throwable) {
                throw IllegalStateException("catch exception when loading $name", cause)
            }
        }
    }
}

private fun unsupportedType(type: Class<*>) {
    throw UnsupportedOperationException(
            "cannot load value of type $type")
}

/**
 * Parses a duration string. If no units are specified in the string, it is
 * assumed to be in milliseconds. The returned duration is in nanoseconds.
 *
 * @param input the string to parse
 *
 * @return duration in nanoseconds
 */
fun parseDuration(input: String): Long {
    val s = ConfigImplUtil.unicodeTrim(input)
    val originalUnitString = getUnits(s)
    var unitString = originalUnitString
    val numberString = ConfigImplUtil.unicodeTrim(s.substring(0, s.length - unitString.length))
    val units: TimeUnit?

    // this would be caught later anyway, but the error message
    // is more helpful if we check it here.
    if (numberString.isEmpty())
        error("No number in duration value '$input'")

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
        error("Could not parse time unit '$originalUnitString' (try ns, us, ms, s, m, h, d)")
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
        error("Could not parse duration number '$numberString'")
    }
}

private fun getUnits(s: String): String {
    var i = s.length - 1
    while (i >= 0) {
        val c = s[i]
        if (!Character.isLetter(c))
            break
        i -= 1
    }
    return s.substring(i + 1)
}

/**
 * Parses a size-in-bytes string. If no units are specified in the string,
 * it is assumed to be in bytes. The returned value is in bytes.
 *
 * @param input the string to parse
 *
 * @return size in bytes
 */
fun parseBytes(input: String): Long {
    val s = ConfigImplUtil.unicodeTrim(input)
    val unitString = getUnits(s)
    val numberString = ConfigImplUtil.unicodeTrim(s.substring(0,
            s.length - unitString.length))

    // this would be caught later anyway, but the error message
    // is more helpful if we check it here.
    if (numberString.isEmpty())
        error("No number in size-in-bytes value '$input'")

    val units = MemoryUnit.parseUnit(unitString) ?:
            error("Could not parse size-in-bytes unit '$unitString' (try k, K, kB, KiB, kilobytes, kibibytes)")

    try {
        val result: BigInteger
        // if the string is purely digits, parse as an integer to avoid
        // possible precision loss; otherwise as a double.
        if (numberString.matches("[0-9]+".toRegex())) {
            result = units.bytes.multiply(BigInteger(numberString))
        } else {
            val resultDecimal = BigDecimal(units.bytes).multiply(BigDecimal(numberString))
            result = resultDecimal.toBigInteger()
        }
        if (result.bitLength() < 64)
            return result.toLong()
        else
            error("size-in-bytes value is out of range for a 64-bit long: '$input'")
    } catch (e: NumberFormatException) {
        error("Could not parse size-in-bytes number '$numberString'")
    }

}

private enum class MemoryUnit constructor(
        internal val prefix: String,
        internal val powerOf: Int,
        internal val power: Int
) {
    BYTES("", 1024, 0),

    KILOBYTES("kilo", 1000, 1),
    MEGABYTES("mega", 1000, 2),
    GIGABYTES("giga", 1000, 3),
    TERABYTES("tera", 1000, 4),
    PETABYTES("peta", 1000, 5),
    EXABYTES("exa", 1000, 6),
    ZETTABYTES("zetta", 1000, 7),
    YOTTABYTES("yotta", 1000, 8),

    KIBIBYTES("kibi", 1024, 1),
    MEBIBYTES("mebi", 1024, 2),
    GIBIBYTES("gibi", 1024, 3),
    TEBIBYTES("tebi", 1024, 4),
    PEBIBYTES("pebi", 1024, 5),
    EXBIBYTES("exbi", 1024, 6),
    ZEBIBYTES("zebi", 1024, 7),
    YOBIBYTES("yobi", 1024, 8);

    internal val bytes: BigInteger = BigInteger.valueOf(powerOf.toLong()).pow(power)

    companion object {

        private fun makeUnitsMap(): Map<String, MemoryUnit> {
            val map = java.util.HashMap<String, MemoryUnit>()
            for (unit in MemoryUnit.values()) {
                map.put(unit.prefix + "byte", unit)
                map.put(unit.prefix + "bytes", unit)
                if (unit.prefix.isEmpty()) {
                    map.put("b", unit)
                    map.put("B", unit)
                    map.put("", unit) // no unit specified means bytes
                } else {
                    val first = unit.prefix.substring(0, 1)
                    val firstUpper = first.toUpperCase()
                    if (unit.powerOf == 1024) {
                        map.put(first, unit)             // 512m
                        map.put(firstUpper, unit)        // 512M
                        map.put(firstUpper + "i", unit)  // 512Mi
                        map.put(firstUpper + "iB", unit) // 512MiB
                    } else if (unit.powerOf == 1000) {
                        if (unit.power == 1) {
                            map.put(first + "B", unit)      // 512kB
                        } else {
                            map.put(firstUpper + "B", unit) // 512MB
                        }
                    } else {
                        throw RuntimeException("broken MemoryUnit enum")
                    }
                }
            }
            return map
        }

        private val unitsMap = makeUnitsMap()

        internal fun parseUnit(unit: String): MemoryUnit? {
            return unitsMap[unit]
        }
    }
}