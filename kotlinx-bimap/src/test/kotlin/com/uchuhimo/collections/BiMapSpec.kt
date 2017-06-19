package com.uchuhimo.collections

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object BiMapSpec : Spek({
    given("an empty bimap") {
        val emptyBiMap = emptyBiMap<Int, String>()

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

    given("a bimap without element") {
        val biMap = biMapOf<Int, String>()

        it("should contain 0 element") {
            assert.that(biMap.isEmpty(), equalTo(true))
        }
    }

    given("a bimap with one element") {
        val biMap = biMapOf(1 to "1")

        it("should contain 1 element") {
            assert.that(biMap.size, equalTo(1))
        }
        it("should contain given key") {
            assert.that(biMap.containsKey(1), equalTo(true))
        }
        it("should contain given value") {
            assert.that(biMap.containsValue("1"), equalTo(true))
        }
    }

    given("a bimap with multiple elements") {
        val biMap = biMapOf(1 to "1", 2 to "2", 3 to "3")

        it("should contain given number of elements") {
            assert.that(biMap.size, equalTo(3))
        }
        it("should contain given keys") {
            assert.that(biMap.keys, hasElement(1) and hasElement(2) and hasElement(3))
        }
        it("should contain given values") {
            assert.that(biMap.values, hasElement("1") and hasElement("2") and hasElement("3"))
        }
        it("should map from key to value as expected") {
            assert.that(biMap[1], equalTo("1"))
        }
        on("inverse") {
            val inverseBiMap = biMap.inverse
            it("should map from key to value as expected") {
                assert.that(inverseBiMap["1"], equalTo(1))
            }
        }
    }
})
