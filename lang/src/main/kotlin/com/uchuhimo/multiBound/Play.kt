package com.uchuhimo.multiBound

interface A

fun <T1 : A, T2> test(t1: T1, t2: T2) where T2 : T1 {}

class A1 : A

class A2 : A

fun main(args: Array<String>) {
    test<A, A2>(A1(), A2())
}
