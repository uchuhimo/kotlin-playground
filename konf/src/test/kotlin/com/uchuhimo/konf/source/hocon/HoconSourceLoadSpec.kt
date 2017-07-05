package com.uchuhimo.konf.source.hocon

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.ConfigForLoad
import com.uchuhimo.konf.source.SourceLoadSpec
import com.uchuhimo.konf.source.load
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object HoconSourceLoadSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            load(HoconProvider.fromString(loadContent))
        }
    }

    itBehavesLike(SourceLoadSpec)
})

private val loadContent = """
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
        year = "2007"
        yearMonth = 2007-12
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
