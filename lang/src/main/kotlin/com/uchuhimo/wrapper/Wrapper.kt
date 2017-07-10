package com.uchuhimo.wrapper

inline fun <T, W> T.wrap(wrapper: T.() -> W): W = wrapper()

inline fun <T, W> T.wrap(wrapper: T.() -> W, block: W.() -> Unit): T {
    wrapper().apply(block)
    return this
}

interface Wrapper {
    fun test(): Int
}

fun main(args: Array<String>) {
    val wrapper: Int.() -> Wrapper = outer@ {
        object : Wrapper {
            override fun test(): Int = this@outer
        }
    }

    1.wrap(wrapper) {
        println(test())
    }

    val wrappedInt = 2.wrap(wrapper)

    println(wrappedInt.test())
}
