package com.uchuhimo.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import com.uchuhimo.konf.source.hocon.HoconProvider
import com.uchuhimo.konf.tempFileOf
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import spark.Spark.get
import spark.Spark.stop
import java.net.URL

object SourceProviderSpec : SubjectSpek<SourceProvider>({
    subject { HoconProvider }

    given("a source provider") {
        on("create source from reader") {
            val source = subject.fromReader("type = reader".reader())
            it("should return a source which contains value from reader") {
                assertThat(source.get("type").toText(), equalTo("reader"))
            }
        }
        on("create source from input stream") {
            val source = subject.fromInputStream(
                    tempFileOf("type = inputStream").inputStream())
            it("should return a source which contains value from input stream") {
                assertThat(source.get("type").toText(), equalTo("inputStream"))
            }
        }
        on("create source from file") {
            val source = subject.fromFile(tempFileOf("type = file"))
            it("should return a source which contains value in file") {
                assertThat(source.get("type").toText(), equalTo("file"))
            }
        }
        on("create source from string") {
            val source = subject.fromString("type = string")
            it("should return a source which contains value in string") {
                assertThat(source.get("type").toText(), equalTo("string"))
            }
        }
        on("create source from byte array") {
            val source = subject.fromBytes("type = bytes".toByteArray())
            it("should return a source which contains value in byte array") {
                assertThat(source.get("type").toText(), equalTo("bytes"))
            }
        }
        on("create source from byte array slice") {
            val source = subject.fromBytes("|type = slice|".toByteArray(), 1, 12)
            it("should return a source which contains value in byte array slice") {
                assertThat(source.get("type").toText(), equalTo("slice"))
            }
        }
        on("create source from HTTP URL") {
            get("/source") { _, _ -> "type = http" }
            val source = subject.fromUrl(URL("http://localhost:4567/source"))
            it("should return a source which contains value in URL") {
                assertThat(source.get("type").toText(), equalTo("http"))
            }
            stop()
        }
        on("create source from file URL") {
            val file = tempFileOf("type = fileUrl")
            val source = subject.fromUrl(file.toURI().toURL())
            it("should return a source which contains value in URL") {
                assertThat(source.get("type").toText(), equalTo("fileUrl"))
            }
        }
        on("create source from resource") {
            val source = subject.fromResource("source/provider.conf")
            it("should return a source which contains value in resource") {
                assertThat(source.get("type").toText(), equalTo("resource"))
            }
        }
        on("create source from non-existed resource") {
            it("should throw SourceNotFoundException") {
                assertThat({ subject.fromResource("source/no-provider.conf") },
                        throws<SourceNotFoundException>())
            }
        }
    }
})