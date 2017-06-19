package com.uchuhimo.konf

class NetworkBuffer {
    companion object : ConfigSpec("network.buffer") {
        val size = required<Int>(name = "size", description = "size of buffer in KB")
        val maxSize = lazy(
                name = "maxSize",
                description = "max size of buffer in KB") { it[size] * 2 }
        val name = optional(
                name = "name",
                default = "buffer",
                description = "name of buffer")
        val type = optional(
                name = "type",
                default = Type.OFF_HEAP,
                description = """
                              | type of network buffer.
                              | two type:
                              | - on-heap
                              | - off-heap
                              | buffer is off-heap by default.
                              """.trimMargin("| "))
    }

    enum class Type {
        ON_HEAP, OFF_HEAP
    }
}