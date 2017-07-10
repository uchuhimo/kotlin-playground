package com.uchuhimo.union

import com.uchuhimo.collections.mutableBiMapOf
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

open class Union {
    private val typeById = mutableMapOf<Int, TaggedType<*>>()

    val types: Collection<TaggedType<*>> get() = typeById.values

    inline fun <reified T : Any> typeOf(id: Int): TaggedType<T> = typeOf(id, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> typeOf(id: Int, kClass: KClass<T>): TaggedType<T> =
            typeById[id]?.let { type ->
                if (type.klass == kClass) {
                    return type as TaggedType<T>
                } else {
                    throw ClassCastException("${type} is not compatible with ${kClass}")
                }
            } ?: throw IllegalArgumentException("type with id ${id} is not in ${this}")

    inline fun <reified T : Any> type() = type(T::class)

    fun <T : Any> type(kClass: KClass<T>): TaggedType<T> =
            TaggedType(kClass, counter).also { type ->
                typeById[type.id] = type
                counter++
            }

    companion object {
        var counter: Int = 0
    }
}

data class TaggedType<T : Any>(val klass: KClass<T>, val id: Int)

open class NamedUnion : Union() {
    private val nameById = mutableBiMapOf<Int, String>()

    fun nameOf(type: TaggedType<*>): String =
            nameById[type.id] ?: throw IllegalArgumentException("${type} is not in ${this}")

    inline fun <reified T : Any> typeOf(name: String): TaggedType<T> = typeOf(name, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> typeOf(name: String, kClass: KClass<T>): TaggedType<T> =
            nameById.inverse[name]?.let { id ->
                typeOf(id, kClass)
            } ?: throw IllegalArgumentException("type with name ${name} is not in ${this}")

    inline fun <reified T : Any> type(name: String) = type(name, T::class)
    fun <T : Any> type(name: String, kClass: KClass<T>): TaggedType<T> =
            type(kClass).also { type ->
                nameById[type.id] = name
            }
}

open class EnumNamedUnion<E : Enum<E>> : NamedUnion() {
    private val enumById = mutableBiMapOf<Int, E>()

    fun enumOf(type: TaggedType<*>): E =
            enumById[type.id] ?: throw IllegalArgumentException("${type} is not in ${this}")

    inline fun <reified T : Any> typeOf(enum: E): TaggedType<T> = typeOf(enum, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> typeOf(enum: E, kClass: KClass<T>): TaggedType<T> =
            enumById.inverse[enum]?.let { id ->
                typeOf(id, kClass)
            } ?: throw IllegalArgumentException("type with enum ${enum.name} is not in ${this}")

    inline fun <reified T : Any> type(enum: E) = type(enum, T::class)
    fun <T : Any> type(enum: E, kClass: KClass<T>): TaggedType<T> =
            type(enum.name, kClass).also { type ->
                enumById[type.id] = enum
            }
}

inline fun <T : Any> on(type: TaggedType<T>, block: (KClass<T>) -> Unit) {
    block(type.klass)
}

object ExampleNamedUnion : NamedUnion() {
    val intType = type<Int>("int")
    val doubleType = type<Double>("double")
    val stringType = type<String>("string")
}

enum class ExampleEnum {
    INT, DOUBLE, STRING
}

fun main(args: Array<String>) {
    val union = object : NamedUnion() {
        val intType = type<Int>("int")
        val doubleType = type<Double>("double")
        val stringType = type<String>("string")
    }
    union.types.forEach { println(it) }
    on(union.intType) {
        println(it)
        println(union.nameOf(union.intType))
    }
    println(union.typeOf<Double>("double"))
    ExampleNamedUnion.types.forEach { println(it) }
    on(ExampleNamedUnion.doubleType) {
        println(it)
    }
    val enumNamedUnion = EnumNamedUnion<ExampleEnum>().apply {
        type<Int>(ExampleEnum.INT)
        type<Double>(ExampleEnum.DOUBLE)
        type<String>(ExampleEnum.STRING)
    }
    enumNamedUnion.types.forEach { println(it) }
    println(enumNamedUnion.typeOf<Int>(ExampleEnum.INT))
    on(enumNamedUnion.typeOf<Double>(ExampleEnum.DOUBLE)) {
        println(it)
    }
    println(enumNamedUnion.enumOf(enumNamedUnion.typeOf<Int>(ExampleEnum.INT)))
}
