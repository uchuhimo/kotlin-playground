package com.uchuhimo.konf

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object ConfigSpek : SubjectSpek<Config>({

    subject { Config { addSpec(NetworkBuffer) } }

    given("a config") {
        val invalidItem = ConfigSpec("invalid").run { required<Int>("invalidItem") }
        group("addSpec operation") {
            on("add orthogonal spec") {
                val spec = object : ConfigSpec(NetworkBuffer.prefix) {
                    val minSize = optional("minSize", 1)
                }
                subject.addSpec(spec)
                it("should contain items in new spec") {
                    assertThat(spec.minSize in subject, equalTo(true))
                    assertThat(spec.minSize.name in subject, equalTo(true))
                }
            }
            on("add repeated item") {
                it("should throw RepeatedItemException") {
                    assertThat({ subject.addSpec(NetworkBuffer) }, throws<RepeatedItemException>())
                }
            }
            on("add repeated name") {
                val spec = ConfigSpec(NetworkBuffer.prefix).apply { required<Int>("size") }
                it("should throw RepeatedNameException") {
                    assertThat({ subject.addSpec(spec) }, throws<RepeatedNameException>())
                }
            }
        }
        group("get operation") {
            on("get with valid item") {
                it("should return corresponding value") {
                    assertThat(subject[NetworkBuffer.name], equalTo("buffer"))
                }
            }
            on("get with invalid item") {
                it("should throw NoSuchElementException when using `get`") {
                    assertThat({ subject[invalidItem] }, throws<NoSuchElementException>())
                }
                it("should throw NoSuchElementException when using `getOrNull`") {
                    assertThat(subject.getOrNull(invalidItem), absent())
                }
            }
            on("get with valid name") {
                it("should return corresponding value") {
                    assertThat(subject<String>("${NetworkBuffer.prefix}.name"), equalTo("buffer"))
                }
            }
            on("get with invalid name") {
                it("should throw NoSuchElementException when using `get`") {
                    assertThat({ subject<String>("${NetworkBuffer.prefix}.invalid") },
                            throws<NoSuchElementException>())
                }
                it("should throw NoSuchElementException when using `getOrNull`") {
                    assertThat(subject.getOrNull<String>("${NetworkBuffer.prefix}.invalid"), absent())
                }
            }
            on("get unset item") {
                it("should throw IllegalStateException") {
                    assertThat({ subject[NetworkBuffer.size] }, throws<IllegalStateException>())
                    assertThat({ subject[NetworkBuffer.maxSize] }, throws<IllegalStateException>())
                }
            }
        }
        group("set operation") {
            on("set with valid item when corresponding value is unset") {
                subject[NetworkBuffer.size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBuffer.size], equalTo(1024))
                }
            }
            on("set with valid item when corresponding value exists") {
                subject[NetworkBuffer.name] = "newName"
                it("should contain the specified value") {
                    assertThat(subject[NetworkBuffer.name], equalTo("newName"))
                }
            }
            on("set with valid item when corresponding value is lazy") {
                test("before set, the item should be lazy; after set," +
                        " the item should be no longer lazy, and it contains the specified value") {
                    subject[NetworkBuffer.size] = 1024
                    assertThat(subject[NetworkBuffer.maxSize],
                            equalTo(subject[NetworkBuffer.size] * 2))
                    subject[NetworkBuffer.maxSize] = 0
                    assertThat(subject[NetworkBuffer.maxSize], equalTo(0))
                    subject[NetworkBuffer.size] = 2048
                    assertThat(subject[NetworkBuffer.maxSize],
                            !equalTo(subject[NetworkBuffer.size] * 2))
                    assertThat(subject[NetworkBuffer.maxSize], equalTo(0))
                }
            }
            on("set with invalid item") {
                it("should throw NoSuchElementException") {
                    assertThat({ subject[invalidItem] = 1024 },
                            throws<NoSuchElementException>())
                }
            }
            on("set with valid name") {
                subject["${NetworkBuffer.prefix}.size"] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBuffer.size], equalTo(1024))
                }
            }
            on("set with invalid name") {
                it("should throw NoSuchElementException") {
                    assertThat({ subject[invalidItem] = 1024 }, throws<NoSuchElementException>())
                }
            }
            on("set with incorrect type of value") {
                it("should throw ClassCastException") {
                    assertThat({ subject[NetworkBuffer.size.name] = "1024" },
                            throws<ClassCastException>())
                }
            }
            on("lazy set with valid item") {
                subject.lazySet(NetworkBuffer.maxSize) { it[NetworkBuffer.size] * 4 }
                subject[NetworkBuffer.size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBuffer.maxSize],
                            equalTo(subject[NetworkBuffer.size] * 4))
                }
            }
            on("lazy set with valid name") {
                subject.lazySet(NetworkBuffer.maxSize.name) { it[NetworkBuffer.size] * 4 }
                subject[NetworkBuffer.size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBuffer.maxSize],
                            equalTo(subject[NetworkBuffer.size] * 4))
                }
            }
            on("lazy set with valid name and invalid value with incompatible type") {
                subject.lazySet(NetworkBuffer.maxSize.name) { "string" }
                it("should throw InvalidLazySetException when getting") {
                    assertThat({ subject[NetworkBuffer.maxSize.name] },
                            throws<InvalidLazySetException>())
                }
            }
            on("unset with valid item") {
                subject.unset(NetworkBuffer.type)
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(NetworkBuffer.type), absent())
                }
            }
            on("unset with valid name") {
                subject.unset(NetworkBuffer.type.name)
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(NetworkBuffer.type), absent())
                }
            }
        }
        group("item property") {
            on("declare a property by item") {
                var name by subject.property(NetworkBuffer.name)
                it("should behave same as `get`") {
                    assertThat(name, equalTo(subject[NetworkBuffer.name]))
                }
                it("should support set operation as `set`") {
                    name = "newName"
                    assertThat(name, equalTo("newName"))
                }
            }
            on("declare a property by name") {
                var name by subject.property<String>(NetworkBuffer.name.name)
                it("should behave same as `get`") {
                    assertThat(name, equalTo(subject[NetworkBuffer.name]))
                }
                it("should support set operation as `set`") {
                    name = "newName"
                    assertThat(name, equalTo("newName"))
                }
            }
        }
    }
})