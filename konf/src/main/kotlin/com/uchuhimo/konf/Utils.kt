package com.uchuhimo.konf

fun <T> unsupported(): T {
    throw UnsupportedOperationException()
}

fun getUnits(s: String): String {
    var i = s.length - 1
    while (i >= 0) {
        val c = s[i]
        if (!c.isLetter())
            break
        i -= 1
    }
    return s.substring(i + 1)
}