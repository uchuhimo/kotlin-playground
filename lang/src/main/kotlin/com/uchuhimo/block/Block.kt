package com.uchuhimo.block

fun main(args: Array<String>) {
    val (outerA, _) = run {
        val a = 1
        Pair(a, 11)
    }
    println(outerA)
    val outerB = fun(): Int {
        val b = 2
        return b
    }
    println(outerB())

    // doc here
    run {
        println("just a block")
    }
    val outerC = {
        3
    }
    println(outerC())
}
