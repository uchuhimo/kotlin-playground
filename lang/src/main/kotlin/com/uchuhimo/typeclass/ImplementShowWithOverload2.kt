package com.uchuhimo.typeclass

object ImplementShowWithOverload2 {
    fun log(a: List<Base>) {
        ImplementShowWithOverload.log(a, object : Show<List<Base>> {
            override fun show(f: List<Base>): String = "base list"
        })
    }

    // since type erasure, cannot overload
    /*
    fun log(a: List<Derived>) {
        ImplementShowWithOverload.log(a, object : Show<List<Derived>> {
            override fun show(f: List<Derived>): String = "base list"
        })
    }
    */
}
