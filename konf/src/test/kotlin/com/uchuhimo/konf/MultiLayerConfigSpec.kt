package com.uchuhimo.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isIn
import com.natpryce.hamkrest.or
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object MultiLayerConfigSpec : SubjectSpek<Config>({

    subject { Config { addSpec(NetworkBuffer) }.withLayer("multi-layer") }

    itBehavesLike(ConfigSpek)

    group("multi-layer config") {
        it("should have specified name") {
            assertThat(subject.name, equalTo("multi-layer"))
        }
        it("should contain same items with parent config") {
            assertThat(subject[NetworkBuffer.name],
                    equalTo(subject.parent!![NetworkBuffer.name]))
            assertThat(subject[NetworkBuffer.type],
                    equalTo(subject.parent!![NetworkBuffer.type]))
        }
        on("set with item") {
            subject[NetworkBuffer.name] = "newName"
            it("should contain the specified value in the top level," +
                    " and keep the rest levels unchanged") {
                assertThat(subject[NetworkBuffer.name], equalTo("newName"))
                assertThat(subject.parent!![NetworkBuffer.name], equalTo("buffer"))
            }
        }
        on("set with name") {
            subject["${NetworkBuffer.prefix}.name"] = "newName"
            it("should contain the specified value in the top level," +
                    " and keep the rest levels unchanged") {
                assertThat(subject[NetworkBuffer.name], equalTo("newName"))
                assertThat(subject.parent!![NetworkBuffer.name], equalTo("buffer"))
            }
        }
        on("add spec") {
            val spec = object : ConfigSpec(NetworkBuffer.prefix) {
                val minSize = optional("minSize", 1)
            }
            subject.addSpec(spec)
            it("should contain items in new spec, and keep the rest level unchanged") {
                assertThat(spec.minSize in subject, equalTo(true))
                assertThat(spec.minSize.name in subject, equalTo(true))
                assertThat(spec.minSize !in subject.parent!!, equalTo(true))
                assertThat(spec.minSize.name !in subject.parent!!, equalTo(true))
            }
        }
        on("iterate items in config after adding spec") {
            val spec = object : ConfigSpec(NetworkBuffer.prefix) {
                val minSize = optional("minSize", 1)
            }
            subject.addSpec(spec)
            it("should cover all items in config") {
                for (item in subject) {
                    assertThat(item, isIn(NetworkBuffer.items) or isIn(spec.items))
                }
                val items = mutableListOf<Item<*>>()
                for (item in subject) {
                    items += item
                }
                for (item in NetworkBuffer.items) {
                    assertThat(item, isIn(items))
                }
                for (item in spec.items) {
                    assertThat(item, isIn(items))
                }
                assertThat(items.size, equalTo(NetworkBuffer.items.size + spec.items.size))
            }
        }
    }
})