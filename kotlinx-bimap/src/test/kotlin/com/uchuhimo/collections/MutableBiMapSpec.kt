package com.uchuhimo.collections

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object MutableBiMapSpec : Spek({
    given("an empty mutable bimap") {
        val emptyBiMap = mutableBiMapOf<Int, String>()

        it("should contain 0 element") {
            assert.that(emptyBiMap.isEmpty(), equalTo(true))
        }

        on("inverse") {
            val inverseBiMap = emptyBiMap.inverse
            it("should contain 0 element") {
                assert.that(inverseBiMap.isEmpty(), equalTo(true))
            }
        }
    }

    given("a mutable bimap") {
        val biMap = mutableBiMapOf(1 to "1", 2 to "2", 3 to "3")

        it("should contain given number of elements") {
            assert.that(biMap.size, equalTo(3))
        }
        it("should contain given keys") {
            assert.that(biMap.keys, hasElement(1) and hasElement(2) and hasElement(3))
        }
        it("should contain given values") {
            assert.that(biMap.values, hasElement("1") and hasElement("2") and hasElement("3"))
        }
        it("should map from given key to given value") {
            assert.that(biMap[1], equalTo("1"))
        }
        on("inverse") {
            val inverseBiMap = biMap.inverse
            it("should map from key to value as expected") {
                assert.that(inverseBiMap["1"], equalTo(1))
            }
        }
        on("inverse twice") {
            it("should return itself") {
                assert.that(biMap.inverse.inverse === biMap, equalTo(true))
            }
        }
        group("put operation") {
            on("put entry, when both key and value are unbound") {
                val previousValue = biMap.put(4, "4")
                it("should contain specified entry") {
                    assert.that(biMap.containsKey(4), equalTo(true))
                    assert.that(biMap.containsValue("4"), equalTo(true))
                    assert.that(biMap[4], equalTo("4"))
                }
                it("should not remove any existing entry") {
                    assert.that(biMap.keys.size, equalTo(4))
                    assert.that(biMap.values.size, equalTo(4))
                    assert.that<String?>(previousValue, equalTo(null))
                }
            }
            on("put entry, when key exists, and value is unbound") {
                biMap[4] = "5"
                it("should contain specified entry") {
                    assert.that(biMap.containsKey(4), equalTo(true))
                    assert.that(biMap.containsValue("5"), equalTo(true))
                    assert.that(biMap[4], equalTo("5"))
                }
                it("should remove previous value") {
                    assert.that(biMap.containsValue("4"), equalTo(false))
                }
            }
            on("put entry, when key is unbound, and value exists") {
                it("should throw IllegalArgumentException") {
                    assert.that({ biMap.put(5, "5") }, throws<IllegalArgumentException>())
                }
            }
            on("force put entry, when key is unbound, and value exists") {
                biMap.forcePut(5, "5")
                it("should contain specified entry") {
                    assert.that(biMap.containsKey(5), equalTo(true))
                    assert.that(biMap.containsValue("5"), equalTo(true))
                    assert.that(biMap[5], equalTo("5"))
                }
                it("should remove previous key") {
                    assert.that(biMap.containsKey(4), equalTo(false))
                }
            }
            on("force put entry, when both key and value exist") {
                val previousValue = biMap.forcePut(5, "5")
                it("should be unchanged") {
                    assert.that(biMap[5], equalTo(previousValue))
                }
            }
            on("put multiple entries") {
                biMap.putAll(mapOf(6 to "6", 7 to "7", 8 to "8"))
                it("should contain these entries") {
                    assert.that(biMap[6], equalTo("6"))
                    assert.that(biMap[7], equalTo("7"))
                    assert.that(biMap[8], equalTo("8"))
                }
            }
        }
        group("remove operation") {
            on("remove existing key") {
                biMap.remove(1)
                it("should not contain the specified key") {
                    assert.that(biMap.containsKey(1), equalTo(false))
                }
            }
            on("remove unbound key") {
                it("doesn't contain the specified key before removing") {
                    assert.that(biMap.containsKey(9), equalTo(false))
                }
                biMap.remove(9)
                it("should not contain the specified key after removing") {
                    assert.that(biMap.containsKey(9), equalTo(false))
                }
            }
            on("remove existing entry") {
                biMap.remove(2, "2")
                it("should not contain the specified entry") {
                    assert.that(biMap.containsKey(2), equalTo(false))
                    assert.that(biMap.containsValue("2"), equalTo(false))
                }
            }
            on("remove entry, when key exists, and value is unbound") {
                biMap.remove(3, "2")
                it("should not remove any entry") {
                    assert.that(biMap.containsKey(3), equalTo(true))
                }
            }
        }
        on("clear") {
            biMap.clear()
            it("should be empty") {
                assert.that(biMap.isEmpty(), equalTo(true))
            }
        }
    }
})