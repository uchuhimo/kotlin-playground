package com.uchuhimo.konf

data class SizeInBytes(val bytes: Long) {
    init {
        check(bytes >= 0)
    }
}