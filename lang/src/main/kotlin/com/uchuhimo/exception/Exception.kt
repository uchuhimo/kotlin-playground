package com.uchuhimo.exception

class MyException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
    constructor(
            message: String,
            cause: Throwable,
            enableSuppression: Boolean,
            writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)
}

@Throws(MyException::class)
fun throwException(flag: Boolean): Int {
    if (flag) {
        return 1
    } else {
        throw MyException()
    }
}

fun main(args: Array<String>) {
    throwException(true)
}
