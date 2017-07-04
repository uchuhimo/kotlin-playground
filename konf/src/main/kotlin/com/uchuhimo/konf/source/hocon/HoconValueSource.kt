package com.uchuhimo.konf.source.hocon

import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType
import com.uchuhimo.konf.Path
import com.uchuhimo.konf.source.ParseException
import com.uchuhimo.konf.source.Source
import com.uchuhimo.konf.source.WrongTypeException

class HoconValueSource(val value: ConfigValue) : Source {
    override val description: String get() = value.origin().description()

    private val type = value.valueType()

    private fun checkType(actual: ConfigValueType, expected: ConfigValueType) {
        if (actual != expected) {
            throw WrongTypeException(this, "HOCON(${actual.name})", "HOCON(${expected.name})")
        }
    }

    enum class NumType {
        Int, Long, Double
    }

    private fun checkNumType(expected: NumType) {
        val unwrappedValue = value.unwrapped()
        val type = when (unwrappedValue) {
            is Int -> NumType.Int
            is Long -> NumType.Long
            is Double -> NumType.Double
            else -> throw ParseException(
                    "value $unwrappedValue with type ${unwrappedValue::class.java.simpleName}" +
                            " is not a valid number(Int/Long/Double)")
        }
        if (type != expected) {
            throw WrongTypeException(this, "HOCON(${type.name})", "HOCON(${expected.name})")
        }
    }

    private val hoconSource: HoconSource by lazy {
        checkType(type, ConfigValueType.OBJECT)
        HoconSource((value as ConfigObject).toConfig())
    }

    override fun contains(path: Path): Boolean = hoconSource.contains(path)

    override fun getOrNull(path: Path): Source? = hoconSource.getOrNull(path)

    override fun toList(): List<Source> {
        checkType(type, ConfigValueType.LIST)
        return mutableListOf<Source>().apply {
            for (value in (value as ConfigList)) {
                add(HoconValueSource(value))
            }
        }
    }

    override fun toMap(): Map<String, Source> {
        checkType(type, ConfigValueType.OBJECT)
        return mutableMapOf<String, Source>().apply {
            for ((key, value) in (value as ConfigObject)) {
                put(key, HoconValueSource(value))
            }
        }
    }

    override fun toText(): String {
        checkType(type, ConfigValueType.STRING)
        return value.unwrapped() as String
    }

    override fun toBoolean(): Boolean {
        checkType(type, ConfigValueType.BOOLEAN)
        return value.unwrapped() as Boolean
    }

    override fun toDouble(): Double {
        try {
            checkType(type, ConfigValueType.NUMBER)
            checkNumType(NumType.Double)
            return value.unwrapped() as Double
        } catch (e: WrongTypeException) {
            try {
                checkNumType(NumType.Long)
                return (value.unwrapped() as Long).toDouble()
            } catch (e: WrongTypeException) {
                checkNumType(NumType.Int)
                return (value.unwrapped() as Int).toDouble()
            }
        }
    }

    override fun toLong(): Long {
        try {
            checkType(type, ConfigValueType.NUMBER)
            checkNumType(NumType.Long)
            return value.unwrapped() as Long
        } catch (e: WrongTypeException) {
            checkNumType(NumType.Int)
            return (value.unwrapped() as Int).toLong()
        }
    }

    override fun toInt(): Int {
        checkType(type, ConfigValueType.NUMBER)
        checkNumType(NumType.Int)
        return value.unwrapped() as Int
    }
}