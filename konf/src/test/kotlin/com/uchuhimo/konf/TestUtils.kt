package com.uchuhimo.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import java.io.File

fun tempFileOf(content: String): File {
    return createTempFile().apply {
        writeText(content)
    }
}

fun assertTrue(actual: Boolean) {
    assertThat(actual, equalTo(true))
}