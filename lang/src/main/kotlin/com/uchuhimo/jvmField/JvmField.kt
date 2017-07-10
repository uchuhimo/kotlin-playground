package com.uchuhimo.jvmField

private val privateVal = 1

private var privateVar = 1

val publicVal = 1

var publicVar = 1

@JvmField
val publicJvmField = 1

@JvmField
var publicJvmFieldVar = 1

@Volatile
var volatileVar = 1

@Volatile
@JvmField
var volatileJvmField = 1

class Test {
    private val privateVal = 1

    private var privateVar = 1

    val publicVal = 1

    var publicVar = 1

    @JvmField
    val publicJvmField = 1

    @JvmField
    var publicJvmFieldVar = 1

    @Volatile
    var volatileVar = 1

    @Volatile
    @JvmField
    var volatileJvmField = 1
}
