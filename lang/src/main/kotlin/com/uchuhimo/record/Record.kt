package com.uchuhimo.record

fun main(args: Array<String>) {
    withTestScope {
        a = 1
        b = 2
        key("ab") to 1
        key("record") to record {
            key("a") to 2
            key("b") to 3
        }
    }
}

class TestScope : Record() {
    var a: Int = 1
    var b: Int = 2
}

open class Record {
    fun key(name: String): Key = Key(name)

    fun key(vararg name: String) {

    }

    infix fun Key.to(value: Int) {
        println("${name}: ${value}")
    }

    infix fun Key.to(value: Record) {
        println("${name}: ${value}")
    }

    companion object {
        data class Key(val name: String)
    }
}

fun record(block: Record.() -> Unit): Record = Record().apply(block)

fun withTestScope(block: TestScope.() -> Unit) {
    TestScope().block()
}