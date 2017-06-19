package com.uchuhimo.konf

import org.jetbrains.spek.api.Spek

object ConfigTestSpec : Spek({
    NetworkBuffer.items.forEach { println(it.name); println(it.description) }
    val config = Config().apply { addSpec(NetworkBuffer) }
    config.apply {
        addSpec(object : ConfigSpec("network.buffer") {
            init {
                optional("name1", 1)
            }
        })
    }
    config[NetworkBuffer.size] = 1024
    config["network.buffer.type"] = NetworkBuffer.Type.ON_HEAP
    println(config[NetworkBuffer.maxSize])
    println(config[NetworkBuffer.name])
    println(config[NetworkBuffer.type])
    println(config.get<NetworkBuffer.Type>("network.buffer.type"))
    println(config<NetworkBuffer.Type>("network.buffer.type"))
    config[NetworkBuffer.size] = 2048
    println(config[NetworkBuffer.maxSize])
    config[NetworkBuffer.maxSize] = 0
    println(config[NetworkBuffer.maxSize])
    config[NetworkBuffer.size] = 1024
    println(config[NetworkBuffer.maxSize])
})