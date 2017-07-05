package com.uchuhimo.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.assertTrue
import com.uchuhimo.konf.source.value.asSource
import com.uchuhimo.konf.toSizeInBytes
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
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

object SourceLoadSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            load(loadContent.asSource())
        }
    }

    given("a source") {
        on("load the source into config") {
            it("should contain every value specified in the source") {
                assertThat(subject[ConfigForLoad.booleanItem], equalTo(false))

                assertThat(subject[ConfigForLoad.intItem], equalTo(1))
                assertThat(subject[ConfigForLoad.shortItem], equalTo(2.toShort()))
                assertThat(subject[ConfigForLoad.byteItem], equalTo(3.toByte()))
                assertThat(subject[ConfigForLoad.bigIntegerItem], equalTo(BigInteger.valueOf(4)))
                assertThat(subject[ConfigForLoad.longItem], equalTo(4L))

                assertThat(subject[ConfigForLoad.doubleItem], equalTo(1.5))
                assertThat(subject[ConfigForLoad.floatItem], equalTo(-1.5f))
                assertThat(subject[ConfigForLoad.bigDecimalItem], equalTo(BigDecimal.valueOf(1.5)))

                assertThat(subject[ConfigForLoad.charItem], equalTo(' '))

                assertThat(subject[ConfigForLoad.stringItem], equalTo("string"))
                assertThat(subject[ConfigForLoad.offsetTimeItem],
                        equalTo(OffsetTime.parse("10:15:30+01:00")))
                assertThat(subject[ConfigForLoad.offsetDateTimeItem],
                        equalTo(OffsetDateTime.parse("2007-12-03T10:15:30+01:00")))
                assertThat(subject[ConfigForLoad.zonedDateTimeItem],
                        equalTo(ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]")))
                assertThat(subject[ConfigForLoad.localDateItem],
                        equalTo(LocalDate.parse("2007-12-03")))
                assertThat(subject[ConfigForLoad.localTimeItem],
                        equalTo(LocalTime.parse("10:15:30")))
                assertThat(subject[ConfigForLoad.localDateTime],
                        equalTo(LocalDateTime.parse("2007-12-03T10:15:30")))
                assertThat(subject[ConfigForLoad.year],
                        equalTo(Year.parse("2007")))
                assertThat(subject[ConfigForLoad.yearMonth],
                        equalTo(YearMonth.parse("2007-12")))
                assertThat(subject[ConfigForLoad.instantTime],
                        equalTo(Instant.parse("2007-12-03T10:15:30.00Z")))
                assertThat(subject[ConfigForLoad.durationTime],
                        equalTo(Duration.parse("P2DT3H4M")))
                assertThat(subject[ConfigForLoad.simpleDurationTime],
                        equalTo(Duration.ofMillis(200)))
                assertThat(subject[ConfigForLoad.sizeItem].bytes, equalTo(10240L))

                assertThat(subject[ConfigForLoad.enumItem], equalTo(EnumForLoad.LABEL2))

                // array items
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.booleanArrayItem],
                        booleanArrayOf(true, false)))
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.intArrayItem],
                        intArrayOf(1, 2, 3)))
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.longArrayItem],
                        longArrayOf(4, 5, 6)))
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.doubleArrayItem],
                        doubleArrayOf(-1.0, 0.0, 1.0)))
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.charArrayItem],
                        charArrayOf('a', 'b', 'c')))

                // object array items
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.booleanObjectArrayItem],
                        arrayOf(true, false)))
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.intObjectArrayItem],
                        arrayOf(1, 2, 3)))
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.stringArrayItem],
                        arrayOf("one", "two", "three")))
                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.enumArrayItem],
                        arrayOf(EnumForLoad.LABEL1, EnumForLoad.LABEL2, EnumForLoad.LABEL3)))

                assertThat(subject[ConfigForLoad.listItem], equalTo(listOf(1, 2, 3)))

                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.mutableListItem].toTypedArray(),
                        arrayOf(1, 2, 3)))

                assertThat(subject[ConfigForLoad.listOfListItem],
                        equalTo(listOf(listOf(1, 2), listOf(3, 4))))

                assertThat(subject[ConfigForLoad.setItem], equalTo(setOf(1, 2)))

                assertThat(subject[ConfigForLoad.sortedSetItem],
                        equalTo<SortedSet<Int>>(sortedSetOf(1, 2, 3)))

                assertThat(subject[ConfigForLoad.mapItem],
                        equalTo(mapOf("a" to 1, "b" to 2, "c" to 3)))
            }
        }
    }
})

private val loadContent = mapOf<String, Any>(
        "boolean" to false,

        "int" to 1,
        "short" to 2.toShort(),
        "byte" to 3.toByte(),
        "bigInteger" to BigInteger.valueOf(4),
        "long" to 4L,

        "double" to 1.5,
        "float" to -1.5f,
        "bigDecimal" to BigDecimal.valueOf(1.5),

        "char" to ' ',

        "string" to "string",
        "offsetTime" to OffsetTime.parse("10:15:30+01:00"),
        "offsetDateTime" to OffsetDateTime.parse("2007-12-03T10:15:30+01:00"),
        "zonedDateTime" to ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"),
        "localDate" to LocalDate.parse("2007-12-03"),
        "localTime" to LocalTime.parse("10:15:30"),
        "localDateTime" to LocalDateTime.parse("2007-12-03T10:15:30"),
        "year" to Year.parse("2007"),
        "yearMonth" to YearMonth.parse("2007-12"),
        "instant" to Instant.parse("2007-12-03T10:15:30.00Z"),
        "duration" to "P2DT3H4M".toDuration(),
        "simpleDuration" to "200millis".toDuration(),
        "size" to "10k".toSizeInBytes(),

        "enum" to "LABEL2",

        "array.boolean" to listOf(true, false),
        "array.int" to listOf(1, 2, 3),
        "array.long" to listOf(4L, 5L, 6L),
        "array.double" to listOf(-1.0, 0.0, 1.0),
        "array.char" to listOf('a', 'b', 'c'),

        "array.object.boolean" to listOf(true, false),
        "array.object.int" to listOf(1, 2, 3),
        "array.object.string" to listOf("one", "two", "three"),
        "array.object.enum" to listOf("LABEL1", "LABEL2", "LABEL3"),

        "list" to listOf(1, 2, 3),
        "mutableList" to listOf(1, 2, 3),
        "listOfList" to listOf(listOf(1, 2), listOf(3, 4)),
        "set" to listOf(1, 2, 1),
        "sortedSet" to listOf(2, 1, 1, 3),

        "map" to mapOf(
                "a" to 1,
                "b" to 2,
                "c" to 3)
).mapKeys { (key, _) -> "level1.level2.$key" }
