package com.uchuhimo.konf.source.env

import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.base.FlatMapSource

object EnvProvider {
    fun fromEnv(): Source {
        return FlatMapSource(System.getenv())
    }
}