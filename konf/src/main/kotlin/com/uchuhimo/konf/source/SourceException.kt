package com.uchuhimo.konf.source

import com.uchuhimo.konf.ConfigException
import com.uchuhimo.konf.Path
import com.uchuhimo.konf.name
import java.io.File

open class SourceException : ConfigException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class WrongTypeException(val source: Source, actual: String, expected: String) :
        SourceException("source ${source.description} has type $actual rather than $expected")

class NoSuchPathException(val source: Source, val path: Path) :
        SourceException("cannot find path \"${path.name}\" in source ${source.description}")

class ParseException : SourceException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class UnsupportedTypeException(val source: Source, val clazz: Class<*>) :
        SourceException("value of type ${clazz.simpleName} is unsupported in source ${source.description}")

class UnsupportedMapKeyException(val clazz: Class<*>) : SourceException(
        "cannot support map with ${clazz.simpleName} key, only support string key")

class LoadException(val path: Path, cause: Throwable) :
        SourceException("fail to load ${path.name}", cause)

class SourceNotFoundException : SourceException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class UnsupportedExtensionException(file: File) : SourceException(
        "cannot detect supported extension for \"${file.name}\"," +
                " supported extensions: conf, json, properties, toml, xml, yml, yaml")