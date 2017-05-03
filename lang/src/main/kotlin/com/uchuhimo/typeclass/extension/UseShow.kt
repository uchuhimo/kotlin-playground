package com.uchuhimo.typeclass.extension

import com.uchuhimo.typeclass.Base
import com.uchuhimo.typeclass.Derived
import com.uchuhimo.typeclass.Show

fun <A> A.log(s: Show<A>) {
    println(s.show(this))
}

fun <A : Show<A>> A.log() {
    this.log(this)
}

fun String.log() {
    this.log(object : Show<String> {
        override fun show(f: String): String = f
    })
}

@JvmName("logForBaseList")
fun List<Base>.log() {
    this.log(object : Show<List<Base>> {
        override fun show(f: List<Base>): String = "base list"
    })
}

@JvmName("logForDerivedList")
fun List<Derived>.log() {
    this.log(object : Show<List<Derived>> {
        override fun show(f: List<Derived>): String = "base list"
    })
}

fun Derived.log() {
    this.log(object : Show<Derived> {
        override fun show(f: Derived): String = f.show(f) + " derived"
    })
}

fun main(args: Array<String>) {
    "test".log()
    listOf(Base(), Base()).log()
    listOf(Derived(), Derived()).log()
    Base().log()
    Derived().log()
}
