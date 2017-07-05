package com.uchuhimo.konf.source.properties

import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.SourceProvider
import com.uchuhimo.konf.source.base.FlatMapSource
import java.io.InputStream
import java.io.Reader
import java.util.*

object PropertiesProvider : SourceProvider {
    private fun Properties.toMap(): Map<String, String> {
        return mapKeys {
            it.key as String
        }.mapValues {
            it.value as String
        }
    }

    override fun fromReader(reader: Reader): Source =
            FlatMapSource(Properties().apply { load(reader) }.toMap())

    override fun fromInputStream(inputStream: InputStream): Source =
            FlatMapSource(Properties().apply { load(inputStream) }.toMap())

    fun fromSystem(): Source = FlatMapSource(System.getProperties().toMap())
}