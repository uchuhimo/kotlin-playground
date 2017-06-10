package com.uchuhimo.extension

class HasExtensionFunction {
    fun Int.test() {
        println(this)
    }
}

fun main(args: Array<String>) {
    val hasExtensionFunction = HasExtensionFunction()
    hasExtensionFunction.apply {
        1.test()
    }
}