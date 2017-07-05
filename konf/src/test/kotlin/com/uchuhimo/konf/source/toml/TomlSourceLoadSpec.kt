package com.uchuhimo.konf.source.toml

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.ConfigForLoad
import com.uchuhimo.konf.source.SourceLoadSpec
import com.uchuhimo.konf.source.load
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object TomlSourceLoadSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            load(TomlProvider.fromResource("source/source.toml"))
        }
    }

    itBehavesLike(SourceLoadSpec)
})
