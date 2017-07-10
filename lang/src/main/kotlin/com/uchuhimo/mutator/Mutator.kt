package com.uchuhimo.mutator

data class PersonInKotlin(var name: String = "", var isDeceased: Boolean = false)

fun main(args: Array<String>) {
    val person = PersonInJava()
    person.name = "test"
    person.isDeceased = true
    println(person.name)
    println(person.isDeceased)
}
