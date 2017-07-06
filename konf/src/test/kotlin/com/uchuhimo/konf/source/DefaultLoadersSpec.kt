package com.uchuhimo.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.tempFileOf
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object DefaultLoadersSpec : SubjectSpek<DefaultLoaders>({
    subject {
        Config {
            addSpec(DefaultLoadersConfig)
        }.loadFrom
    }

    given("a loader") {
        on("load from system environment") {
            val config = subject.env()
            it("should return a config which contains value from system environment") {
                assertThat(config[DefaultLoadersConfig.type], equalTo("env"))
            }
        }
        on("load from system properties") {
            System.setProperty(DefaultLoadersConfig.qualify("type"), "system")
            val config = subject.systemProperties()
            it("should return a config which contains value from system properties") {
                assertThat(config[DefaultLoadersConfig.type], equalTo("system"))
            }
        }
        group("load from file") {
            on("load from file with .conf extension") {
                val config = subject.file(
                        tempFileOf("source.test.type = hocon", suffix = ".conf"))
                it("should load as HOCON format") {
                    assertThat(config[DefaultLoadersConfig.type], equalTo("hocon"))
                }
            }
            on("load from file with .json extension") {
                val config = subject.file(
                        tempFileOf("""
{
  "source": {
    "test": {
      "type": "json"
    }
  }
}
""",
                                suffix = ".json"))
                it("should load as JSON format") {
                    assertThat(config[DefaultLoadersConfig.type], equalTo("json"))
                }
            }
            on("load from file with .properties extension") {
                val config = subject.file(
                        tempFileOf("source.test.type = properties", suffix = ".properties"))
                it("should load as properties file format") {
                    assertThat(config[DefaultLoadersConfig.type], equalTo("properties"))
                }
            }
            on("load from file with .toml extension") {
                val config = subject.file(
                        tempFileOf("""
[source.test]
type = "toml"
""",
                                suffix = ".toml"))
                it("should load as TOML format") {
                    assertThat(config[DefaultLoadersConfig.type], equalTo("toml"))
                }
            }
            on("load from file with .xml extension") {
                val config = subject.file(
                        tempFileOf("""
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property>
        <name>source.test.type</name>
        <value>xml</value>
    </property>
</configuration>
""".trim(),
                                suffix = ".xml"))
                it("should load as XML format") {
                    assertThat(config[DefaultLoadersConfig.type], equalTo("xml"))
                }
            }
            on("load from file with .yaml extension") {
                val config = subject.file(
                        tempFileOf("""
source:
    test:
        type: yaml
""",
                                suffix = ".yaml"))
                it("should load as YAML format") {
                    assertThat(config[DefaultLoadersConfig.type], equalTo("yaml"))
                }
            }
            on("load from file with .yml extension") {
                val config = subject.file(
                        tempFileOf("""
source:
    test:
        type: yml
""",
                                suffix = ".yml"))
                it("should load as YAML format") {
                    assertThat(config[DefaultLoadersConfig.type], equalTo("yml"))
                }
            }
            on("load from file with unsupported extension") {
                it("should throw UnsupportedExtensionException") {
                    assertThat({
                        subject.file(tempFileOf("source.test.type = txt", suffix = ".txt"))
                    }, throws<UnsupportedExtensionException>())
                }
            }
        }
    }
})

private object DefaultLoadersConfig : ConfigSpec("source.test") {
    val type = required<String>("type")
}