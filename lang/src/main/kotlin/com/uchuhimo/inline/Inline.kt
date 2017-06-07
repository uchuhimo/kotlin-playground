package com.uchuhimo.inline

inline fun testInline() {
    println("testAtLeastOnce")
}

fun main(args: Array<String>) {
    testInline()
}