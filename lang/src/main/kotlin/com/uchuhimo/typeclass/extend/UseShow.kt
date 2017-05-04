package com.uchuhimo.typeclass.extend

import com.uchuhimo.typeclass.Base
import com.uchuhimo.typeclass.Derived

interface Loggable {
    fun show(): String

    fun log() {
        println(show())
    }
}

abstract class LoggableExtend<out T>(self: T) : Loggable, Self<T>(self)

class LoggableClass : Loggable {
    override fun show(): String = "loggable"
}

val loggableForString: String.() -> LoggableExtend<String> = outer@ {
    object : LoggableExtend<String>(this) {
        override fun show(): String = this@outer
    }
}

fun String.log() {
    this.loggableForString().log()
}

val loggableForBaseList: List<Base>.() -> LoggableExtend<List<Base>> = {
    object : LoggableExtend<List<Base>>(this) {
        override fun show(): String = "base list"
    }
}

@JvmName("logForBaseList")
fun List<Base>.log() {
    this.loggableForBaseList().log()
}

val loggableForDerivedList: List<Derived>.() -> LoggableExtend<List<Derived>> = {
    object : LoggableExtend<List<Derived>>(this) {
        override fun show(): String = "derived list"
    }
}

@JvmName("logForDerivedList")
fun List<Derived>.log() {
    this.loggableForDerivedList().log()
}

interface Walker {
    fun walk()
}

abstract class WalkerExtend<out T>(self: T) : Walker, Self<T>(self)

val walkerForString: String.() -> WalkerExtend<String> = {
    object : WalkerExtend<String>(this) {
        override fun walk() {
            println("walk: $self")
        }
    }
}

fun main(args: Array<String>) {
    "test".extend(loggableForString).log()
    "test".loggableForString().log()
    "test".log()
    listOf(Base(), Base()).loggableForBaseList().log()
    listOf(Base(), Base()).log()
    listOf(Derived(), Derived()).loggableForDerivedList().log()
    listOf(Derived(), Derived()).loggableForBaseList().log()
    listOf(Derived(), Derived()).log()
    LoggableClass().log()

    walkerForString("test").walk()
    "test".extend(walkerForString).walk()
    val extendString = "test".extend {
        object : Self<String>(this),
                Walker by walkerForString(this),
                Loggable by loggableForString(this) {}
    }
    extendString.log()
    extendString.walk()
    println(extendString.self.length)
    val multiExtend = { it: String ->
        object : Self<String>(it),
                Walker by walkerForString(it),
                Loggable by loggableForString(it) {}
    }
    "test".extend(multiExtend).walk()
}
