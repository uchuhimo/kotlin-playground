package com.uchuhimo.variance

class Test<in In, out Out> {
    fun acceptIn(parameter: In) {}

    fun acceptPair(pair: Pair<In, Int>) {
        println(pair.first)
    }

    // 'in' occurs in 'out'
    // fun acceptFunc1(func: (In) -> Int) {}

    fun acceptFunc2(func: (Out) -> Int) {}

    fun acceptFunc3(func: () -> In) {}

    // 'out' occurs in 'in'
    // fun acceptFunc4(func: () -> Out) {}

    fun acceptList(list: List<In>) {}

    // 'in' occurs in 'invariant'
    // fun acceptMutableList(list: MutableList<In>) {}

    // use-site variance
    fun acceptMutableList(list: MutableList<out In>) {
        list.forEach { println(it) }
        val set = list.toSet()
        // can't use 'out In' for 'in'
        // list.addAll(set)
    }

    // use-site variance
    fun acceptMutableOutList(list: MutableList<in Out?>) {
        list.add(null)
        // can't use 'in Out?' for 'out'
        // type of 'it' is 'Any?'
        // list.forEach { it: Out? -> println(it) }
    }

    fun returnOut(): Out? = null

    fun returnPair(): Pair<Out, Int>? = null

    fun returnFunc1(): () -> Out? = { null }

    // 'in' occurs in 'out'
    // fun returnFunc2(): () -> In? = { null }

    // 'out' occurs in 'in'
    // fun returnFunc3(): (Out) -> Int = { _ -> 1 }

    fun returnFunc4(): (In) -> Int = { _ -> 1 }

    fun returnList(): List<Out>? = null

    // 'out' occurs in 'invariant'
    // fun returnMutableList(): MutableList<Out>? = null

    // use-site variance
    fun returnMutableList(): MutableList<out Out?> {
        val list = mutableListOf<Out?>()
        list.add(null)
        list.forEach { println(it) }
        return list
    }

    // use-site variance
    fun returnMutableInList(): MutableList<in In?> {
        val list = mutableListOf<In?>()
        list.add(null)
        list.forEach { println(it) }
        return list
    }
}

fun main(args: Array<String>) {
    val test = Test<String, Double>()

    val list = test.returnMutableList()
    // can't use 'out Double?' for 'in'
    // list.add(1.0)

    val inList = test.returnMutableInList()
    // can't use 'in In?' for 'out'
    // type of 'it' is 'Any?'
    // inList.forEach { it: Double? -> println(it) }
}
