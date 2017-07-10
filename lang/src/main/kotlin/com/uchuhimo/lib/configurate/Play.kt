package com.uchuhimo.lib.configurate

import ninja.leaping.configurate.gson.GsonConfigurationLoader
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.json.JSONConfigurationLoader
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.yaml.snakeyaml.DumperOptions
import java.io.File

fun testYaml() {
    val loader = YAMLConfigurationLoader.builder().apply {
        setFile(File("test.yml"))
        setFlowStyle(DumperOptions.FlowStyle.BLOCK)
    }.build()
    val root = loader.createEmptyNode()
    val node = root.getNode("network", "buffer", "size")
    node.value = 1024
    loader.save(root)
}

fun readYaml() {
    val loader = YAMLConfigurationLoader.builder().apply {
        setFile(File("test.yml"))
        setFlowStyle(DumperOptions.FlowStyle.BLOCK)
    }.build()
    val root = loader.load()
    val node = root.getNode("network", "buffer", "size")
    println(node.value)
}

fun testHocon() {
    val loader = HoconConfigurationLoader.builder().apply {
        setFile(File("test.conf"))
    }.build()
    val root = loader.createEmptyNode()
    val node = root.getNode("network", "buffer", "size")
    node.value = 1024
    val comment = "size of network buffer"
    node.setComment(Array(100) { comment }.reduce(String::plus))
    loader.save(root)
}

fun readHocon() {
    val loader = HoconConfigurationLoader.builder().apply {
        setFile(File("test.conf"))
    }.build()
    val root = loader.load()
    val node = root.getNode("network", "buffer", "size")
    println(node.value)
}

fun testJson() {
    val loader = JSONConfigurationLoader.builder().apply {
        setFile(File("test.json"))
    }.build()
    val root = loader.createEmptyNode()
    val node = root.getNode("network", "buffer", "size")
    node.value = 1024
    node.getNode("comment").value = "size of network buffer"
    loader.save(root)
}

fun readJson() {
    val loader = JSONConfigurationLoader.builder().apply {
        setFile(File("test.json"))
    }.build()
    val root = loader.load()
    val node = root.getNode("network", "buffer", "size")
    println(node.getNode("comment").value)
}

fun testGson() {
    val loader = GsonConfigurationLoader.builder().apply {
        setFile(File("test.gson"))
    }.build()
    val root = loader.createEmptyNode()
    val node = root.getNode("network", "buffer", "size")
    node.value = 1024
    loader.save(root)
}

fun main(args: Array<String>) {
    testYaml()
    testHocon()
    testJson()
    testGson()
    readJson()
    readYaml()
    readHocon()
}
