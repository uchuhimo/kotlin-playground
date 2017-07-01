@file:JvmName("Configs")

package com.uchuhimo.konf

private fun generateItemDoc(
        item: Item<*>,
        key: String = item.name,
        separator: String = " = ",
        encode: (Any) -> String
): String =
        StringBuilder().apply {
            item.description.lines().forEach { line ->
                appendln("# $line")
            }
            if (item is LazyItem) {
                appendln("# default: ${item.placeholder}")
            }
            append(key)
            append(separator)
            if (item is OptionalItem) {
                append(encode(item.default))
            }
            appendln()
        }.toString()

fun Config.generatePropertiesDoc(): String =
        StringBuilder().apply {
            for (item in this@generatePropertiesDoc) {
                append(generateItemDoc(item, encode = Any::toString))
                appendln()
            }
        }.toString()

private fun encodeAsHocon(value: Any): String =
        when (value) {
            is Int -> value.toString()
            is String -> "\"$value\""
            is Enum<*> -> "\"${value.name}\""
            else -> value.toString()
        }

fun Config.generateHoconDoc(): String =
        StringBuilder().apply {
            visitAsTree(
                    onEnterNode = { path ->
                        if (path.size >= 1) {
                            append(" ".repeat(4 * (path.size - 1)))
                            appendln("${path.last()} {")
                        }
                    },
                    onLeaveNode = { path ->
                        if (path.size >= 1) {
                            append(" ".repeat(4 * (path.size - 1)))
                            appendln("}")
                        }
                    },
                    onEnterLeaf = { path, item ->
                        generateItemDoc(
                                item,
                                key = item.path.last(),
                                encode = ::encodeAsHocon
                        ).lines().forEach { line ->
                            append(" ".repeat(4 * (path.size - 1)))
                            append(line)
                            appendln()
                        }
                    }
            )
        }.toString()

fun Config.generateYamlDoc(): String =
        StringBuilder().apply {
            visitAsTree(
                    onEnterNode = { path ->
                        if (path.size >= 1) {
                            append(" ".repeat(4 * (path.size - 1)))
                            appendln("${path.last()}:")
                        }
                    },
                    onEnterLeaf = { path, item ->
                        generateItemDoc(
                                item,
                                key = item.path.last(),
                                separator = ": ",
                                encode = ::encodeAsHocon
                        ).lines().forEach { line ->
                            append(" ".repeat(4 * (path.size - 1)))
                            append(line)
                            appendln("")
                        }
                    }
            )
        }.toString()

fun Config.generateTomlDoc(): String =
        StringBuilder().apply {
            specs.forEach { spec ->
                appendln("[${spec.prefix}]")
                spec.items.forEach { item ->
                    append(generateItemDoc(item, key = item.path.last(), encode = ::encodeAsHocon))
                    appendln()
                }
            }
        }.toString()

fun Config.generateXmlDoc(): String =
        StringBuilder().apply {
            appendln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendln("<configuration>")
            for (item in this@generateXmlDoc) {
                appendln("  <property>")
                appendln("    <name>${item.name}</name>")
                append("    <value>")
                if (item is OptionalItem) {
                    append(item.default.toString())
                } else if (item is LazyItem) {
                    append("<!-- ${item.placeholder} -->")
                }
                appendln("</value>")
                appendln("    <description>")
                item.description.lines().forEach { line ->
                    appendln("      $line")
                }
                appendln("    </description>")
                appendln("  </property>")
            }
            appendln("</configuration>")
        }.toString()
