package com.uchuhimo.konf.source.json

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.ConfigForLoad
import com.uchuhimo.konf.source.SourceLoadSpec
import com.uchuhimo.konf.source.load
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object JsonSourceLoadSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            load(JsonProvider.fromResource("source/source.json"))
        }
    }

    itBehavesLike(SourceLoadSpec)
})
