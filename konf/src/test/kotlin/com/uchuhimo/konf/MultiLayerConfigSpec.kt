package com.uchuhimo.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
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
            subject[NetworkBuffer.name.name] = "newName"
            it("should contain the specified value in the top level," +
                    " and keep the rest levels unchanged") {
                assertThat(subject[NetworkBuffer.name], equalTo("newName"))
                assertThat(subject.parent!![NetworkBuffer.name], equalTo("buffer"))
            }
        }
        on("set parent's value") {
            subject.parent!![NetworkBuffer.name] = "newName"
            it("should contain the specified value in both top and parent level") {
                assertThat(subject[NetworkBuffer.name], equalTo("newName"))
                assertThat(subject.parent!![NetworkBuffer.name], equalTo("newName"))
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
        on("add spec to parent") {
            val spec = object : ConfigSpec(NetworkBuffer.prefix) {
                val minSize = optional("minSize", 1)
            }
            it("should throw SpecFrozenException") {
                assertThat({ subject.parent!!.addSpec(spec) }, throws<SpecFrozenException>())
            }
        }
        on("iterate items in config after adding spec") {
            val spec = object : ConfigSpec(NetworkBuffer.prefix) {
                val minSize = optional("minSize", 1)
            }
            subject.addSpec(spec)
            it("should cover all items in config") {
                assertThat(subject.iterator().asSequence().toSet(),
                        equalTo((NetworkBuffer.items + spec.items).toSet()))
            }
        }
    }
})