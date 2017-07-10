package com.uchuhimo.datamodel

import java.util.Arrays
import kotlin.reflect.KClass

interface InputStream {
    fun readInt(): Int
    fun readIntArray(): IntArray
    fun <T : Any> read(type: KClass<T>): T
}

interface OutputStream {
    fun writeInt(value: Int)
    fun writeIntArray(value: IntArray)
    fun <T : Any> write(value: T, type: KClass<out T>)
}

class InputStreamImpl : InputStream {
    override fun readInt(): Int {
        return 1
    }

    override fun readIntArray(): IntArray {
        return intArrayOf(0)
    }

    override fun <T : Any> read(type: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ID(2, 3) as T
    }
}

class OutputStreamImpl : OutputStream {
    override fun writeInt(value: Int) {
        println("int: $value")
    }

    override fun writeIntArray(value: IntArray) {
        println("int array: ${Arrays.toString(value)}")
    }

    override fun <T : Any> write(value: T, type: KClass<out T>) {
        println("object: $value")
    }
}

interface Serializer<T> {
    fun serialize(value: T, outputStream: OutputStream)
}

interface Deserializer<T> {
    fun deserialize(inputStream: InputStream): T
}

interface Serde<T> : Serializer<T>, Deserializer<T>

class IntSerde : Serde<Int> {
    override fun serialize(value: Int, outputStream: OutputStream) {
        outputStream.writeInt(value)
    }

    override fun deserialize(inputStream: InputStream): Int {
        return inputStream.readInt()
    }
}

class IntArraySerde : Serde<IntArray> {
    override fun serialize(value: IntArray, outputStream: OutputStream) {
        outputStream.writeIntArray(value)
    }

    override fun deserialize(inputStream: InputStream): IntArray {
        return inputStream.readIntArray()
    }
}

abstract class ObjectSerde<T>(
        private val serializeActions: List<(T, OutputStream) -> Unit>) : Serde<T> {
    override fun serialize(value: T, outputStream: OutputStream) {
        serializeActions.forEach { action -> action(value, outputStream) }
    }
}

class ImmutableSerde<T>(
        serializeActions: List<(T, OutputStream) -> Unit>,
        private val deserializeActions: List<(InputStream) -> Any>,
        private val create: (List<Any>) -> T) : ObjectSerde<T>(serializeActions) {

    override fun deserialize(inputStream: InputStream): T {
        return create(deserializeActions.map { action -> action(inputStream) })

    }
}

class MutableSerde<T>(
        serializeActions: List<(T, OutputStream) -> Unit>,
        private val deserializeActions: List<(T, InputStream) -> Unit>,
        private val create: () -> T) : ObjectSerde<T>(serializeActions) {

    override fun deserialize(inputStream: InputStream): T {
        val value = create()
        deserializeActions.forEach { action -> action(value, inputStream) }
        return value
    }
}

data class ID(val group: Int, val value: Int)

data class WrappedArray(val array: IntArray, val length: Int, val id: ID)

fun serialize(wrappedArray: WrappedArray, outputStream: OutputStream) {
    outputStream.writeIntArray(wrappedArray.array)
    outputStream.writeInt(wrappedArray.length)
    outputStream.write(wrappedArray.id, ID::class)
}

fun deserialize(inputStream: InputStream): WrappedArray {
    val array = inputStream.readIntArray()
    val length = inputStream.readInt()
    val id = inputStream.read(ID::class)
    return WrappedArray(array, length, id)
}

sealed class FieldModel<T>

sealed class ImmutableModel<T> : FieldModel<T>()

sealed class MutableModel<T> : FieldModel<T>()

data class ImmutableIntModel<T>(val getter: (T) -> Int) : ImmutableModel<T>()

data class MutableIntModel<T>(val getter: (T) -> Int, val setter: (T, Int) -> Unit) : MutableModel<T>()

data class ImmutableIntArrayModel<T>(val getter: (T) -> IntArray) : ImmutableModel<T>()

data class MutableIntArrayModel<T>(val getter: (T) -> IntArray, val setter: (T, IntArray) -> Unit) : MutableModel<T>()

data class ImmutableObjectModel<T, ObjectType : Any>(val type: KClass<ObjectType>, val getter: (T) -> ObjectType) : ImmutableModel<T>()

data class MutableObjectModel<T, ObjectType : Any>(val type: KClass<ObjectType>, val getter: (T) -> ObjectType, val setter: (T, ObjectType) -> Unit) : MutableModel<T>()

sealed class ParentModel<T>

data class ImmutableParentModel<T>(val create: (List<Any>) -> T, val fields: List<ImmutableModel<T>>) : ParentModel<T>()

data class MutableParentModel<T>(val create: () -> T, val fields: List<MutableModel<T>>) : ParentModel<T>()

fun <T> modelToSerde(model: ParentModel<T>): Serde<T> = when (model) {
    is ImmutableParentModel<T> -> {
        ImmutableSerde(
                serializeActions = model.fields.map { field ->
                    when (field) {
                        is ImmutableIntModel<T> ->
                            { instance: T, outputStream: OutputStream ->
                                outputStream.writeInt(field.getter(instance))
                            }
                        is ImmutableIntArrayModel<T> ->
                            { instance, outputStream ->
                                outputStream.writeIntArray(field.getter(instance))
                            }
                        is ImmutableObjectModel<T, *> ->
                            { instance, outputStream ->
                                val value = field.getter(instance)
                                outputStream.write(value, value::class)
                            }
                    }
                },
                deserializeActions = model.fields.map { field ->
                    when (field) {
                        is ImmutableIntModel<T> ->
                            { inputStream: InputStream -> inputStream.readInt() }
                        is ImmutableIntArrayModel<T> ->
                            { inputStream -> inputStream.readIntArray() }
                        is ImmutableObjectModel<T, *> ->
                            { inputStream -> inputStream.read(field.type) }
                    }
                },
                create = model.create
        )
    }
    else -> TODO()
}

class ImmutableContext<T> {
    private lateinit var create: (List<Any>) -> T
    private val fields: MutableList<ImmutableModel<T>> = mutableListOf()

    fun create(func: (List<Any>) -> T) {
        create = func
    }

    fun withInt(getter: (T) -> Int) {
        fields += ImmutableIntModel(getter)
    }

    fun withIntArray(getter: (T) -> IntArray) {
        fields += ImmutableIntArrayModel(getter)
    }

    fun <ObjectType : Any> withObject(type: KClass<ObjectType>, getter: (T) -> ObjectType) {
        fields += ImmutableObjectModel(type, getter)
    }

    fun toModel(): ImmutableParentModel<T> = ImmutableParentModel(create, fields)
}

fun <T> immutable(init: ImmutableContext<T>.() -> Unit): ImmutableParentModel<T> = ImmutableContext<T>().apply(init).toModel()

fun main(args: Array<String>) {
    val wrappedArray = WrappedArray(intArrayOf(0), 1, ID(2, 3))
    val outputStream = OutputStreamImpl()
    val inputStream = InputStreamImpl()
    serialize(wrappedArray, outputStream)
    serialize(deserialize(inputStream), outputStream)
    val serde = ImmutableSerde(
            serializeActions = arrayListOf(
                    { wrappedArray, outputStream -> outputStream.writeIntArray(wrappedArray.array) },
                    { wrappedArray, outputStream -> outputStream.writeInt(wrappedArray.length) },
                    { wrappedArray, outputStream -> outputStream.write(wrappedArray.id, wrappedArray.id::class) }
            ),
            deserializeActions = arrayListOf(
                    { inputStream -> inputStream.readIntArray() },
                    { inputStream -> inputStream.readInt() },
                    { inputStream -> inputStream.read(ID::class) }
            ),
            create = { args -> WrappedArray(args[0] as IntArray, args[1] as Int, args[2] as ID) }
    )
    serde.serialize(serde.deserialize(inputStream), outputStream)
    val wrapperArrayModel1 = ImmutableParentModel(
            create = { args -> WrappedArray(args[0] as IntArray, args[1] as Int, args[2] as ID) },
            fields = arrayListOf(
                    ImmutableIntArrayModel(WrappedArray::array),
                    ImmutableIntModel(WrappedArray::length),
                    ImmutableObjectModel(ID::class, WrappedArray::id)
            )
    )
    val serde1 = modelToSerde(wrapperArrayModel1)
    serde1.serialize(serde1.deserialize(inputStream), outputStream)

    val wrapperArrayModel2 = immutable<WrappedArray> {
        withIntArray(WrappedArray::array)
        withInt(WrappedArray::length)
        withObject(ID::class, WrappedArray::id)
        create { WrappedArray(it[0] as IntArray, it[1] as Int, it[2] as ID) }
    }
    val serde2 = modelToSerde(wrapperArrayModel2)
    serde2.serialize(serde2.deserialize(inputStream), outputStream)
}
