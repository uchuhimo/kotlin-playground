package com.uchuhimo.konf

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isIn
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object ConfigInJavaSpec : SubjectSpek<Config>({

    subject { Config { addSpec(NetworkBufferInJava.spec) } }

    given("a config") {
        val invalidItem = ConfigSpec("invalid").run { required<Int>("invalidItem") }
        group("addSpec operation") {
            on("add orthogonal spec") {
                val spec = object : ConfigSpec(NetworkBufferInJava.spec.prefix) {
                    val minSize = optional("minSize", 1)
                }
                subject.addSpec(spec)
                it("should contain items in new spec") {
                    assertThat(spec.minSize in subject, equalTo(true))
                    assertThat(spec.minSize.name in subject, equalTo(true))
                }
                it("should contain new spec") {
                    assertThat(spec in subject.specs, equalTo(true))
                    assertThat(NetworkBufferInJava.spec in subject.specs, equalTo(true))
                }
            }
            on("add repeated item") {
                it("should throw RepeatedItemException") {
                    assertThat({ subject.addSpec(NetworkBufferInJava.spec) }, throws<RepeatedItemException>())
                }
            }
            on("add repeated name") {
                val spec = ConfigSpec(NetworkBufferInJava.spec.prefix).apply { required<Int>("size") }
                it("should throw NameConflictException") {
                    assertThat({ subject.addSpec(spec) }, throws<NameConflictException>())
                }
            }
            on("add conflict name, which is prefix of existed name") {
                val spec = ConfigSpec("network").apply { required<Int>("buffer") }
                it("should throw NameConflictException") {
                    assertThat({ subject.addSpec(spec) }, throws<NameConflictException>())
                }
            }
            on("add conflict name, and an existed name is prefix of it") {
                val spec = ConfigSpec(NetworkBufferInJava.type.name).apply {
                    required<Int>("subType")
                }
                it("should throw NameConflictException") {
                    assertThat({ subject.addSpec(spec) }, throws<NameConflictException>())
                }
            }
        }
        on("iterate items in config") {
            it("should cover all items in config") {
                for (item in subject) {
                    assertThat(item, isIn(NetworkBufferInJava.spec.items))
                }
                val items = mutableListOf<Item<*>>()
                for (item in subject) {
                    items += item
                }
                for (item in NetworkBufferInJava.spec.items) {
                    assertThat(item, isIn(items))
                }
                assertThat(items.size, equalTo(NetworkBufferInJava.spec.items.size))
            }
        }
        group("get operation") {
            on("get with valid item") {
                it("should return corresponding value") {
                    assertThat(subject[NetworkBufferInJava.name], equalTo("buffer"))
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
                    assertThat(subject<String>("${NetworkBufferInJava.spec.prefix}.name"), equalTo("buffer"))
                }
            }
            on("get with invalid name") {
                it("should throw NoSuchElementException when using `get`") {
                    assertThat({ subject<String>("${NetworkBufferInJava.spec.prefix}.invalid") },
                            throws<NoSuchElementException>())
                }
                it("should throw NoSuchElementException when using `getOrNull`") {
                    assertThat(subject.getOrNull<String>("${NetworkBufferInJava.spec.prefix}.invalid"), absent())
                }
            }
            on("get unset item") {
                it("should throw IllegalStateException") {
                    assertThat({ subject[NetworkBufferInJava.size] }, throws<IllegalStateException>())
                    assertThat({ subject[NetworkBufferInJava.maxSize] }, throws<IllegalStateException>())
                }
            }
        }
        group("set operation") {
            on("set with valid item when corresponding value is unset") {
                subject[NetworkBufferInJava.size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBufferInJava.size], equalTo(1024))
                }
            }
            on("set with valid item when corresponding value exists") {
                subject[NetworkBufferInJava.name] = "newName"
                it("should contain the specified value") {
                    assertThat(subject[NetworkBufferInJava.name], equalTo("newName"))
                }
            }
            on("set with valid item when corresponding value is lazy") {
                test("before set, the item should be lazy; after set," +
                        " the item should be no longer lazy, and it contains the specified value") {
                    subject[NetworkBufferInJava.size] = 1024
                    assertThat(subject[NetworkBufferInJava.maxSize],
                            equalTo(subject[NetworkBufferInJava.size] * 2))
                    subject[NetworkBufferInJava.maxSize] = 0
                    assertThat(subject[NetworkBufferInJava.maxSize], equalTo(0))
                    subject[NetworkBufferInJava.size] = 2048
                    assertThat(subject[NetworkBufferInJava.maxSize],
                            !equalTo(subject[NetworkBufferInJava.size] * 2))
                    assertThat(subject[NetworkBufferInJava.maxSize], equalTo(0))
                }
            }
            on("set with invalid item") {
                it("should throw NoSuchElementException") {
                    assertThat({ subject[invalidItem] = 1024 },
                            throws<NoSuchElementException>())
                }
            }
            on("set with valid name") {
                subject["${NetworkBufferInJava.spec.prefix}.size"] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBufferInJava.size], equalTo(1024))
                }
            }
            on("set with invalid name") {
                it("should throw NoSuchElementException") {
                    assertThat({ subject[invalidItem] = 1024 }, throws<NoSuchElementException>())
                }
            }
            on("set with incorrect type of value") {
                it("should throw ClassCastException") {
                    assertThat({ subject[NetworkBufferInJava.size.name] = "1024" },
                            throws<ClassCastException>())
                }
            }
            on("lazy set with valid item") {
                subject.lazySet(NetworkBufferInJava.maxSize) { it[NetworkBufferInJava.size] * 4 }
                subject[NetworkBufferInJava.size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBufferInJava.maxSize],
                            equalTo(subject[NetworkBufferInJava.size] * 4))
                }
            }
            on("lazy set with valid name") {
                subject.lazySet(NetworkBufferInJava.maxSize.name) { it[NetworkBufferInJava.size] * 4 }
                subject[NetworkBufferInJava.size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[NetworkBufferInJava.maxSize],
                            equalTo(subject[NetworkBufferInJava.size] * 4))
                }
            }
            on("lazy set with valid name and invalid value with incompatible type") {
                subject.lazySet(NetworkBufferInJava.maxSize.name) { "string" }
                it("should throw InvalidLazySetException when getting") {
                    assertThat({ subject[NetworkBufferInJava.maxSize.name] },
                            throws<InvalidLazySetException>())
                }
            }
            on("unset with valid item") {
                subject.unset(NetworkBufferInJava.type)
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(NetworkBufferInJava.type), absent())
                }
            }
            on("unset with valid name") {
                subject.unset(NetworkBufferInJava.type.name)
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(NetworkBufferInJava.type), absent())
                }
            }
        }
        group("item property") {
            on("declare a property by item") {
                var name by subject.property(NetworkBufferInJava.name)
                it("should behave same as `get`") {
                    assertThat(name, equalTo(subject[NetworkBufferInJava.name]))
                }
                it("should support set operation as `set`") {
                    name = "newName"
                    assertThat(name, equalTo("newName"))
                }
            }
            on("declare a property by name") {
                var name by subject.property<String>(NetworkBufferInJava.name.name)
                it("should behave same as `get`") {
                    assertThat(name, equalTo(subject[NetworkBufferInJava.name]))
                }
                it("should support set operation as `set`") {
                    name = "newName"
                    assertThat(name, equalTo("newName"))
                }
            }
        }
    }
})