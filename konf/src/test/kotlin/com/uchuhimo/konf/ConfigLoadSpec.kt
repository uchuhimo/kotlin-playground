package com.uchuhimo.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isIn
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
import java.time.ZonedDateTime
import java.util.*

object ConfigLoadSpec : SubjectSpek<Config>({

    subject { Config { addSpec(ConfigForLoad) } }

    given("a config") {
        on("load HOCON file") {
            subject.loadHoconFile(tempFileOf(loadContent))
            it("should contain every value specified in file") {
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

                assertThat(subject[ConfigForLoad.listItem].size, equalTo(3))
                assertThat(subject[ConfigForLoad.listItem][0], equalTo(1))
                assertThat(subject[ConfigForLoad.listItem][1], equalTo(2))
                assertThat(subject[ConfigForLoad.listItem][2], equalTo(3))

                assertTrue(Arrays.equals(
                        subject[ConfigForLoad.mutableListItem].toTypedArray(),
                        arrayOf(1, 2, 3)))

                assertThat(subject[ConfigForLoad.listOfListItem][0][0], equalTo(1))
                assertThat(subject[ConfigForLoad.listOfListItem][0][1], equalTo(2))
                assertThat(subject[ConfigForLoad.listOfListItem][1][0], equalTo(3))
                assertThat(subject[ConfigForLoad.listOfListItem][1][1], equalTo(4))

                assertThat(subject[ConfigForLoad.setItem].size, equalTo(2))
                assertThat(1, isIn(subject[ConfigForLoad.setItem]))
                assertThat(2, isIn(subject[ConfigForLoad.setItem]))

                assertThat(subject[ConfigForLoad.sortedSetItem].size, equalTo(3))
                assertThat(subject[ConfigForLoad.sortedSetItem].first(), equalTo(1))
                assertThat(subject[ConfigForLoad.sortedSetItem].last(), equalTo(3))

                assertThat(subject[ConfigForLoad.mapItem].size, equalTo(3))
                assertThat(subject[ConfigForLoad.mapItem]["a"], equalTo(1))
                assertThat(subject[ConfigForLoad.mapItem]["b"], equalTo(2))
                assertThat(subject[ConfigForLoad.mapItem]["c"], equalTo(3))
            }
        }
    }
})

val loadContent = """
level1 {
    level2 {
        boolean = false

        int = 1
        short = 2
        byte = 3
        bigInteger = 4
        long = 4

        double = 1.5
        float = -1.5
        bigDecimal = 1.5

        char = " "

        string = string
        offsetTime = "10:15:30+01:00"
        offsetDateTime = "2007-12-03T10:15:30+01:00"
        zonedDateTime = "2007-12-03T10:15:30+01:00[Europe/Paris]"
        localDate = 2007-12-03
        localTime = "10:15:30"
        localDateTime = "2007-12-03T10:15:30"
        instant = "2007-12-03T10:15:30.00Z"
        duration = P2DT3H4M
        simpleDuration = 200millis
        size = 10k

        enum = LABEL2

        array {
            boolean = [true, false]
            int = [1, 2, 3]
            long = [4, 5, 6]
            double = [-1, 0.0, 1]
            char = [a, b, c]

            object {
                boolean = [true, false]
                int = [1, 2, 3]
                string = [one, two, three]
                enum = [LABEL1, LABEL2, LABEL3]
            }
        }

        list = [1, 2, 3]
        mutableList = [1, 2, 3]
        listOfList = [[1, 2], [3, 4]]
        set = [1, 2, 1]
        sortedSet = [2, 1, 1, 3]

        map = { a = 1, b = 2, c = 3 }
    }
}
"""
