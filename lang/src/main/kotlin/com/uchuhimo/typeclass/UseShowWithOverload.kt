package com.uchuhimo.typeclass

import com.uchuhimo.typeclass.ImplementShowWithOverload.log
import com.uchuhimo.typeclass.ImplementShowWithOverload2.log

fun main(args: Array<String>) {
    log("testAtLeastOnce")
    log(listOf(Base(), Base()))
    log(listOf(Derived(), Derived()))
}
