package com.uchuhimo.interop

class Test {
    companion object {
        const val constField: Int = 1
        val field: Int = 1
        @JvmField val jvmField: Int = 1

        object TopLevel {
            object SecondLevel {
                const val field: Int = 1
            }
        }
    }

    object TopLevel {
        object SecondLevel {
            const val field: Int = 1
        }
    }

    val field: Int = 1

    inner class InnerConfig {
        val field: Int = 1 + this@Test.field

        inner class TopLevel {
            val field: Int = 1 + this@InnerConfig.field

            inner class SecondLevel {
                val field: Int = 1 + this@TopLevel.field
            }
        }
    }

    class Config {
        class TopLevel {
            class SecondLevel {
                companion object {
                    const val field: Int = 1
                }
            }
        }
    }
}

object TopLevel {
    object SecondLevel {
        const val field: Int = 1
    }
}

class Config {
    class TopLevel {
        class SecondLevel {
            companion object {
                const val field: Int = 1
            }
        }
    }
}

fun main(args: Array<String>) {
    Test.constField
    Test.jvmField
    Test.field
    Test.Companion.TopLevel.SecondLevel.field
    Test.TopLevel.SecondLevel.field
    Test.Config.TopLevel.SecondLevel.field
    Test().InnerConfig().TopLevel().SecondLevel().field
}
