package com.uchuhimo.typeclass

import com.uchuhimo.typeclass.implement1.log
import com.uchuhimo.typeclass.implement2.log
import com.uchuhimo.typeclass.implement3.log

val Int.show get() = object : Show<Int> {
    override fun show(f: Int): String = "int"
}

fun main(args: Array<String>) {
    fun log(a: String) {
        com.uchuhimo.typeclass.implement1.log(a)
    }

    log("test")
    log(listOf(Base(), Base()))
    log(listOf(Derived(), Derived()))
    log(Derived())
    log(1, 1.show)
}
