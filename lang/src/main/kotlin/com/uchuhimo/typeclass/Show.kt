package com.uchuhimo.typeclass

interface Show<in A> {
    fun show(f: A): String
}
