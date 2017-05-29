package com.uchuhimo.lateinit

interface Serializer {
    fun serialize(input: Int): Boolean
}

class SerializerImpl : Serializer {
    override fun serialize(input: Int): Boolean {
        println(input)
        return true
    }
}

class SerializerThunk(
        private val init: () -> Serializer,
        private val replace: (Serializer) -> Unit) : Serializer {
    override fun serialize(input: Int): Boolean {
        val serializer = init()
        replace(serializer)
        println("replace")
        return serializer.serialize(input)
    }
}

class Test {
    private var serializer: Serializer = SerializerThunk(::SerializerImpl) { serializer = it }

    fun test() {
        serializer.serialize(1)
    }
}

fun main(args: Array<String>) {
    val test = Test()
    test.test()
    test.test()
}
