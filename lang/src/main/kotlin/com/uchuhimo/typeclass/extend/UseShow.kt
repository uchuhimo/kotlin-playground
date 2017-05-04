package com.uchuhimo.typeclass.extend

import com.uchuhimo.typeclass.Base
import com.uchuhimo.typeclass.Derived

interface Loggable {
    fun show(): String

    fun log() {
        println(show())
    }
}

abstract class LoggableExtend<out T>(self: T) : Self<T>(self), Loggable

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

abstract class WalkerExtend<out T>(self: T) : Self<T>(self), Walker

val walkerForString: String.() -> WalkerExtend<String> = {
    object : WalkerExtend<String>(this) {
        override fun walk() {
            println("walk: $self")
        }
    }
}

abstract class LoggableWalkerExtend<out T>(self: T) : Self<T>(self), Loggable, Walker

fun <T> mix(
        loggable: T.() -> LoggableExtend<T>,
        walker: T.() -> WalkerExtend<T>)
        : T.() -> LoggableWalkerExtend<T> = {
    object : LoggableWalkerExtend<T>(this),
            Loggable by loggable(this),
            Walker by walker(this) {}
}

fun main(args: Array<String>) {
    "test".extend(loggableForString).log()
    "test".loggableForString().log()
    "test".log()

    listOf(Base(), Base()).loggableForBaseList().log()
    listOf(Base(), Base()).log()

    listOf(Derived(), Derived()).loggableForDerivedList().log()
    listOf(Derived(), Derived()).log()
    listOf(Derived(), Derived()).extend(loggableForBaseList).log()
    listOf(Derived(), Derived()).loggableForBaseList().log()

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

    val extendString2 = "test".extend(mix(loggableForString, walkerForString))
    extendString2.log()
    extendString2.walk()
    println(extendString2.self.length)

    val multiExtend = { self: String ->
        object : Self<String>(self),
                Walker by walkerForString(self),
                Loggable by loggableForString(self) {}
    }
    "test".extend(multiExtend).walk()

    val multiExtend2 = { self: String ->
        object : Self<String>(self), Walker, Loggable {
            override fun show(): String = self

            override fun walk() {
                println("walk: $self")
            }
        }
    }
    "test".extend(multiExtend2).log()
}
