package com.uchuhimo.typeclass.implement3

import com.uchuhimo.typeclass.Derived
import com.uchuhimo.typeclass.Show
import com.uchuhimo.typeclass.implement1.log

fun log(a: Derived) {
    log(a, object : Show<Derived> {
        override fun show(f: Derived): String = f.show(f) + " derived"
    })
}

fun <A : Show<A>> log(a: A) {
    log(a, a)
}
