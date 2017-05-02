package com.uchuhimo.typeclass.implement1

import com.uchuhimo.typeclass.Show

fun <A> log(a: A, s: Show<A>) {
    println(s.show(a))
}

fun log(a: String) {
    log(a, object : Show<String> {
        override fun show(f: String): String = f
    })
}
