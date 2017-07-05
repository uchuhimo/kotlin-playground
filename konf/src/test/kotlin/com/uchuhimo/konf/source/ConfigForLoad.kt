package com.uchuhimo.konf.source

import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.SizeInBytes
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
import java.util.*

object ConfigForLoad : ConfigSpec("level1.level2") {
    val booleanItem = required<Boolean>("boolean")

    val intItem = required<Int>("int")
    val shortItem = required<Short>("short")
    val byteItem = required<Byte>("byte")
    val bigIntegerItem = required<BigInteger>("bigInteger")
    val longItem = required<Long>("long")

    val doubleItem = required<Double>("double")
    val floatItem = required<Float>("float")
    val bigDecimalItem = required<BigDecimal>("bigDecimal")

    val charItem = required<Char>("char")

    val stringItem = required<String>("string")
    val offsetTimeItem = required<OffsetTime>("offsetTime")
    val offsetDateTimeItem = required<OffsetDateTime>("offsetDateTime")
    val zonedDateTimeItem = required<ZonedDateTime>("zonedDateTime")
    val localDateItem = required<LocalDate>("localDate")
    val localTimeItem = required<LocalTime>("localTime")
    val localDateTime = required<LocalDateTime>("localDateTime")
    val year = required<Year>("year")
    val yearMonth = required<YearMonth>("yearMonth")
    val instantTime = required<Instant>("instant")
    val durationTime = required<Duration>("duration")
    val simpleDurationTime = required<Duration>("simpleDuration")
    val sizeItem = required<SizeInBytes>("size")

    val enumItem = required<EnumForLoad>("enum")

    // array items
    val booleanArrayItem = required<BooleanArray>("array.boolean")
    val intArrayItem = required<IntArray>("array.int")
    val longArrayItem = required<LongArray>("array.long")
    val doubleArrayItem = required<DoubleArray>("array.double")
    val charArrayItem = required<CharArray>("array.char")

    // object array item
    val booleanObjectArrayItem = required<Array<Boolean>>("array.object.boolean")
    val intObjectArrayItem = required<Array<Int>>("array.object.int")
    val stringArrayItem = required<Array<String>>("array.object.string")
    val enumArrayItem = required<Array<EnumForLoad>>("array.object.enum")

    val listItem = required<List<Int>>("list")
    val mutableListItem = required<List<Int>>("mutableList")
    val listOfListItem = required<List<List<Int>>>("listOfList")
    val setItem = required<Set<Int>>("set")
    val sortedSetItem = required<SortedSet<Int>>("sortedSet")

    val mapItem = required<Map<String, Int>>("map")
}

enum class EnumForLoad {
    LABEL1, LABEL2, LABEL3
}