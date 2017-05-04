package com.uchuhimo.typeclass.extend

open class Self<out T>(val self: T)

infix fun <T, Extend : Self<T>> T.extend(provider: T.() -> Extend): Extend = this.provider()