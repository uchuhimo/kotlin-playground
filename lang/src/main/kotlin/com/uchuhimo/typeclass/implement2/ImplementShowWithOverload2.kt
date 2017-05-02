package com.uchuhimo.typeclass.implement2

import com.uchuhimo.typeclass.Base
import com.uchuhimo.typeclass.Show
import com.uchuhimo.typeclass.implement1.log

fun log(a: String) {
    log(a, object : Show<String> {
        override fun show(f: String): String = f + " override"
    })
}

fun log(a: List<Base>) {
    log(a, object : Show<List<Base>> {
        override fun show(f: List<Base>): String = "base list"
    })
}

// since type erasure, cannot overload
/*
fun log(a: List<Derived>) {
    ImplementShowWithOverload.log(a, object : Show<List<Derived>> {
        override fun show(f: List<Derived>): String = "base list"
    })
}
*/
