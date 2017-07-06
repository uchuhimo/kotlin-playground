package com.uchuhimo.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isIn
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

object ConfigSpecSpek : Spek({
    given("a configSpec") {
        val spec = NetworkBuffer
        it("should name items correctly") {
            assertThat(spec.size.name, equalTo(spec.qualify("size")))
        }
        it("should contain all specified items") {
            assertThat(spec.size, isIn(spec.items))
            assertThat(spec.maxSize, isIn(spec.items))
            assertThat(spec.name, isIn(spec.items))
            assertThat(spec.type, isIn(spec.items))
        }
    }
})