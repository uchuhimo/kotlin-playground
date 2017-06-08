package com.uchuhimo.typeclass

open class Base : Show<Base> {
    override fun show(f: Base): String = "visible"
}
