package com.uchuhimo.konf

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isIn
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object ConfigTestSpec : Spek({
    given("a configSpec") {
        val spec = NetworkBuffer
        it("should name items correctly") {
            assertThat(spec.size.name, equalTo("${spec.prefix}.size"))
        }
        it("should contain all specified items") {
            assertThat(spec.size, isIn(spec.items))
            assertThat(spec.maxSize, isIn(spec.items))
            assertThat(spec.name, isIn(spec.items))
            assertThat(spec.type, isIn(spec.items))
        }
    }
    given("a config") {
        val config by memoized { Config { addSpec(NetworkBuffer) } }
        val invalidItem = ConfigSpec("invalid").run { required<Int>("invalidItem") }
        group("addSpec operation") {
            on("add orthogonal spec") {
                val spec = object : ConfigSpec(NetworkBuffer.prefix) {
                    val minSize = optional("minSize", 1)
                }
                config.addSpec(spec)
                it("should contain items in new spec") {
                    assertThat(spec.minSize in config, equalTo(true))
                    assertThat(spec.minSize.name in config, equalTo(true))
                }
            }
            on("add repeated item") {
                it("should throw RepeatedItemException") {
                    assertThat({ config.addSpec(NetworkBuffer) }, throws<RepeatedItemException>())
                }
            }
            on("add repeated name") {
                val spec = ConfigSpec(NetworkBuffer.prefix).apply { required<Int>("size") }
                it("should throw RepeatedNameException") {
                    assertThat({ config.addSpec(spec) }, throws<RepeatedNameException>())
                }
            }
        }
        group("get operation") {
            on("get with valid item") {
                it("should return corresponding value") {
                    assertThat(config[NetworkBuffer.name], equalTo("buffer"))
                }
            }
            on("get with invalid item") {
                it("should throw NoSuchElementException when using `get`") {
                    assertThat({ config[invalidItem] }, throws<NoSuchElementException>())
                }
                it("should throw NoSuchElementException when using `getOrNull`") {
                    assertThat(config.getOrNull(invalidItem), absent())
                }
            }
            on("get with valid name") {
                it("should return corresponding value") {
                    assertThat(config<String>("${NetworkBuffer.prefix}.name"), equalTo("buffer"))
                }
            }
            on("get with invalid name") {
                it("should throw NoSuchElementException when using `get`") {
                    assertThat({ config<String>("${NetworkBuffer.prefix}.invalid") },
                            throws<NoSuchElementException>())
                }
                it("should throw NoSuchElementException when using `getOrNull`") {
                    assertThat(config.getOrNull<String>("${NetworkBuffer.prefix}.invalid"), absent())
                }
            }
            on("get unset item") {
                it("should throw IllegalStateException") {
                    assertThat({ config[NetworkBuffer.size] }, throws<IllegalStateException>())
                    assertThat({ config[NetworkBuffer.maxSize] }, throws<IllegalStateException>())
                }
            }
        }
        group("set operation") {
            on("set with valid item when corresponding value is unset") {
                config[NetworkBuffer.size] = 1024
                it("should contain the specified value") {
                    assertThat(config[NetworkBuffer.size], equalTo(1024))
                }
            }
            on("set with valid item when corresponding value exists") {
                config[NetworkBuffer.name] = "newName"
                it("should contain the specified value") {
                    assertThat(config[NetworkBuffer.name], equalTo("newName"))
                }
            }
            on("set with valid item when corresponding value is lazy") {
                test("before set, the item should be lazy; after set," +
                        " the item should be no longer lazy, and it contains the specified value") {
                    config[NetworkBuffer.size] = 1024
                    assertThat(config[NetworkBuffer.maxSize], equalTo(2048))
                    config[NetworkBuffer.maxSize] = 0
                    assertThat(config[NetworkBuffer.maxSize], equalTo(0))
                    config[NetworkBuffer.size] = 2048
                    assertThat(config[NetworkBuffer.maxSize], !equalTo(4096))
                }
            }
            on("set with invalid item") {
                it("should throw NoSuchElementException") {
                    assertThat({ config[invalidItem] = 1024 },
                            throws<NoSuchElementException>())
                }
            }
            on("set with valid name") {
                config["${NetworkBuffer.prefix}.size"] = 1024
                it("should contain the specified value") {
                    assertThat(config[NetworkBuffer.size], equalTo(1024))
                }
            }
            on("set with invalid name") {
                it("should throw NoSuchElementException") {
                    assertThat({ config[invalidItem] = 1024 }, throws<NoSuchElementException>())
                }
            }
            on("set with incorrect type of value") {
                it("should throw ClassCastException") {
                    assertThat({ config[NetworkBuffer.size.name] = "1024" },
                            throws<ClassCastException>())
                }
            }
        }
        group("multi-layer config") {
            val multiLayerConfig by memoized { config.withLayer("multi-layer") }
            it("should have specified name") {
                assertThat(multiLayerConfig.name, equalTo("multi-layer"))
            }
            it("should contain same items with parent config") {
                assertThat(multiLayerConfig[NetworkBuffer.name],
                        equalTo(config[NetworkBuffer.name]))
                assertThat(multiLayerConfig[NetworkBuffer.type],
                        equalTo(config[NetworkBuffer.type]))
            }
            on("set with item") {
                multiLayerConfig[NetworkBuffer.name] = "newName"
                it("should contain the specified value in the top level," +
                        " and keep the rest levels unchanged") {
                    assertThat(multiLayerConfig[NetworkBuffer.name], equalTo("newName"))
                    assertThat(config[NetworkBuffer.name], equalTo("buffer"))
                }
            }
            on("set with name") {
                multiLayerConfig["${NetworkBuffer.prefix}.name"] = "newName"
                it("should contain the specified value in the top level," +
                        " and keep the rest levels unchanged") {
                    assertThat(multiLayerConfig[NetworkBuffer.name], equalTo("newName"))
                    assertThat(config[NetworkBuffer.name], equalTo("buffer"))
                }
            }
            on("add spec") {
                val spec = object : ConfigSpec(NetworkBuffer.prefix) {
                    val minSize = optional("minSize", 1)
                }
                multiLayerConfig.addSpec(spec)
                it("should contain items in new spec, and keep the rest level unchanged") {
                    assertThat(spec.minSize in multiLayerConfig, equalTo(true))
                    assertThat(spec.minSize.name in multiLayerConfig, equalTo(true))
                    assertThat(spec.minSize !in config, equalTo(true))
                    assertThat(spec.minSize.name !in config, equalTo(true))
                }
            }
        }
    }
})