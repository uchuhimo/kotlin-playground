package com.uchuhimo.privateAccess

class A(private val a: Int) {
    fun accessOtherA(otherA: A) {
        println(otherA.a)
    }
}

class B<out T : Number>(private var b: T) {
    fun accessOtherB(otherB: B<Int>) {
        // `b` is private to `this` since variance
        // otherB.b
        println(otherB.getB())
    }

    fun getB(): T = b
}

fun main(args: Array<String>) {
    val a1 = A(1)
    val a2 = A(2)
    a1.accessOtherA(a2)
    val b1 = B(1)
    val b2 = B(2)
    b1.accessOtherB(b2)
}
