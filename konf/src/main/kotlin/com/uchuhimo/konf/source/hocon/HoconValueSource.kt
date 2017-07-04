package com.uchuhimo.konf.source.hocon

import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType
import com.uchuhimo.konf.Path
import com.uchuhimo.konf.source.ParseException
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.SourceType
import com.uchuhimo.konf.source.WrongTypeException
import com.uchuhimo.konf.source.checkType

class HoconValueSource(val value: ConfigValue) : Source {
    override val description: String get() = value.origin().description()

    private val hoconSource: HoconSource by lazy {
        checkType(SourceType.Map)
        HoconSource((value as ConfigObject).toConfig())
    }

    override fun contains(path: Path): Boolean = hoconSource.contains(path)

    override fun getOrNull(path: Path): Source? = hoconSource.getOrNull(path)

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    override val type: SourceType = when (value.valueType()) {
        ConfigValueType.OBJECT -> SourceType.Map
        ConfigValueType.LIST -> SourceType.List
        ConfigValueType.NUMBER -> {
            val unwrappedValue = value.unwrapped()
            when (unwrappedValue) {
                is Int -> SourceType.Int
                is Long -> SourceType.Long
                is Double -> SourceType.Double
                else -> throw ParseException(
                        "value $unwrappedValue with type ${unwrappedValue::class.java.simpleName}" +
                                " is not a valid number(Int/Long/Double)")
            }
        }
        ConfigValueType.BOOLEAN -> SourceType.Boolean
        ConfigValueType.STRING -> SourceType.String
        ConfigValueType.NULL -> error("")
    }

    override fun toList(): List<Source> {
        checkType(SourceType.List)
        return mutableListOf<Source>().apply {
            for (value in (value as ConfigList)) {
                add(HoconValueSource(value))
            }
        }
    }

    override fun toMap(): Map<String, Source> {
        checkType(SourceType.Map)
        return mutableMapOf<String, Source>().apply {
            for ((key, value) in (value as ConfigObject)) {
                put(key, HoconValueSource(value))
            }
        }
    }

    override fun toText(): String {
        checkType(SourceType.String)
        return value.unwrapped() as String
    }

    override fun toBoolean(): Boolean {
        checkType(SourceType.Boolean)
        return value.unwrapped() as Boolean
    }

    override fun toDouble(): Double {
        try {
            checkType(SourceType.Double)
            return value.unwrapped() as Double
        } catch (e: WrongTypeException) {
            try {
                checkType(SourceType.Long)
                return (value.unwrapped() as Long).toDouble()
            } catch (e: WrongTypeException) {
                checkType(SourceType.Int)
                return (value.unwrapped() as Int).toDouble()
            }
        }
    }

    override fun toLong(): Long {
        try {
            checkType(SourceType.Long)
            return value.unwrapped() as Long
        } catch (e: WrongTypeException) {
            checkType(SourceType.Int)
            return (value.unwrapped() as Int).toLong()
        }
    }

    override fun toInt(): Int {
        checkType(SourceType.Int)
        return value.unwrapped() as Int
    }
}