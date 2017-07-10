package com.uchuhimo.typeclass.extend

open class Self<out T>(val self: T)

inline infix fun <T, Extend : Self<T>> T.extend(provider: T.() -> Extend): Extend = this.provider()
