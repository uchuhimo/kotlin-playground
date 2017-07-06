package com.uchuhimo.konf.source.map

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.ConfigForLoad
import com.uchuhimo.konf.source.SourceLoadSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object FlatSourceLoadSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
        }.loadFrom.map.flat(loadContent)
    }

    itBehavesLike(SourceLoadSpec)
})

private val loadContent = mapOf(
        "boolean" to "false",
        "int" to "1",
        "short" to "2",
        "byte" to "3",
        "bigInteger" to "4",
        "long" to "4",
        "double" to "1.5",
        "float" to "-1.5",
        "bigDecimal" to "1.5",
        "char" to "a",
        "string" to "string",
        "offsetTime" to "10:15:30+01:00",
        "offsetDateTime" to "2007-12-03T10:15:30+01:00",
        "zonedDateTime" to "2007-12-03T10:15:30+01:00[Europe/Paris]",
        "localDate" to "2007-12-03",
        "localTime" to "10:15:30",
        "localDateTime" to "2007-12-03T10:15:30",
        "date" to "2007-12-03T10:15:30Z",
        "year" to "2007",
        "yearMonth" to "2007-12",
        "instant" to "2007-12-03T10:15:30.00Z",
        "duration" to "P2DT3H4M",
        "simpleDuration" to "200millis",
        "size" to "10k",
        "enum" to "LABEL2",
        "list.0" to "1",
        "list.1" to "2",
        "list.2" to "3",
        "mutableList.0" to "1",
        "mutableList.1" to "2",
        "mutableList.2" to "3",
        "listOfList.0.0" to "1",
        "listOfList.0.1" to "2",
        "listOfList.1.0" to "3",
        "listOfList.1.1" to "4",
        "set.0" to "1",
        "set.1" to "2",
        "set.2" to "1",
        "sortedSet.0" to "2",
        "sortedSet.1" to "1",
        "sortedSet.2" to "1",
        "sortedSet.3" to "3",
        "map.a" to "1",
        "map.b" to "2",
        "map.c" to "3",
        "sortedMap.c" to "3",
        "sortedMap.b" to "2",
        "sortedMap.a" to "1",
        "nested.0.0.0.a" to "1",
        "listOfMap.0.a" to "1",
        "listOfMap.0.b" to "2",
        "listOfMap.1.a" to "3",
        "listOfMap.1.b" to "4",
        "array.boolean.0" to "true",
        "array.boolean.1" to "false",
        "array.int.0" to "1",
        "array.int.1" to "2",
        "array.int.2" to "3",
        "array.long.0" to "4",
        "array.long.1" to "5",
        "array.long.2" to "6",
        "array.double.0" to "-1",
        "array.double.1" to "0.0",
        "array.double.2" to "1",
        "array.char.0" to "a",
        "array.char.1" to "b",
        "array.char.2" to "c",
        "array.object.boolean.0" to "true",
        "array.object.boolean.1" to "false",
        "array.object.int.0" to "1",
        "array.object.int.1" to "2",
        "array.object.int.2" to "3",
        "array.object.string.0" to "one",
        "array.object.string.1" to "two",
        "array.object.string.2" to "three",
        "array.object.enum.0" to "LABEL1",
        "array.object.enum.1" to "LABEL2",
        "array.object.enum.2" to "LABEL3"
).mapKeys { (key, _) -> "level1.level2.$key" }
