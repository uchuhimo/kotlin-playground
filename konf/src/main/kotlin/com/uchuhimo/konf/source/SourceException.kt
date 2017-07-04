package com.uchuhimo.konf.source

import com.uchuhimo.konf.ConfigException
import com.uchuhimo.konf.Path
import com.uchuhimo.konf.name

open class SourceException : ConfigException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class WrongTypeException(source: Source, actual: String, expected: String) :
        SourceException("${source.description} has type $actual rather than $expected")

class NoSuchPathException(source: Source, path: Path) :
        SourceException("cannot find path \"${path.name}\" in ${source.description}")

class ParseException : SourceException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class UnsupportedTypeException(source: Source, clazz: Class<*>) :
        SourceException("value of type ${clazz.simpleName} is unsupported in ${source.description}")

class UnsupportedMapKeyException(clazz: Class<*>) : SourceException(
        "cannot support map with ${clazz.simpleName} key, only support string key")

class LoadException(path: Path, cause: Throwable) :
        SourceException("fail to load ${path.name}", cause)