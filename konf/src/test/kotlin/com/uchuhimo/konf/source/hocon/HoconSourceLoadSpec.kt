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
            load(HoconProvider.fromResource("source/source.conf"))
        }
    }

    itBehavesLike(SourceLoadSpec)
})

