package com.uchuhimo.typeclass.extend

import com.uchuhimo.typeclass.Base
import com.uchuhimo.typeclass.Derived
import com.uchuhimo.typeclass.Show

interface Loggable {
    fun show(): String

    fun log() {
        println(show())
    }
}

class LoggableClass : Loggable {
    override fun show(): String = "loggable"
}

infix fun <T> T.extend(s: Show<T>): Loggable = object : Loggable {
    override fun show(): String = s.show(this@extend)
}

val showString: Show<String> get() = object : Show<String> {
    override fun show(f: String): String = f
}

val String.extendShow get() = this extend showString

fun String.log() {
    this.extendShow.log()
}

val showBaseList: Show<List<Base>> get() = object : Show<List<Base>> {
    override fun show(f: List<Base>): String = "base list"
}

val List<Base>.extendShow
    @JvmName("extendShowForBaseList")
    get() = this extend showBaseList

@JvmName("logForBaseList")
fun List<Base>.log() {
    this.extendShow.log()
}

val showDerivedList: Show<List<Derived>> get() = object : Show<List<Derived>> {
    override fun show(f: List<Derived>): String = "derived list"
}

val List<Derived>.extendShow
    @JvmName("extendShowForDerivedList")
    get() = this extend showDerivedList

@JvmName("logForDerivedList")
fun List<Derived>.log() {
    this.extendShow.log()
}

interface Walker {
    fun walk()
}

interface Walk<Self> {
    fun walk(self: Self)
}

infix fun <T> T.extend(w: Walk<T>): Walker = object : Walker {
    override fun walk() {
        w.walk(this@extend)
    }
}

val walkForString: Walk<String> = object : Walk<String> {
    override fun walk(self: String) {
        println("walk: $self")
    }
}

interface LoggableWalker : Loggable, Walker

fun <T> T.extend(s: Show<T>, w: Walk<T>): LoggableWalker =
        object : LoggableWalker,
                Loggable by extend(s),
                Walker by extend(w) {}

fun main(args: Array<String>) {
    "test".extend(showString).log()
    "test".extendShow.log()
    "test".log()
    listOf(Base(), Base()).extendShow.log()
    listOf(Base(), Base()).log()
    listOf(Derived(), Derived()).extendShow.log()
    listOf(Derived(), Derived()).log()
    LoggableClass().log()

    "test".extend(walkForString).walk()
    val extendString = "test".extend(showString, walkForString)
    extendString.log()
    extendString.walk()
}
