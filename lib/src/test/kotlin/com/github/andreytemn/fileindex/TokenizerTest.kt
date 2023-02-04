package com.github.andreytemn.fileindex

import org.junit.Test
import kotlin.test.assertContentEquals

private const val TEXT = "Lorem Ipsum   Dolor\tSit\r\nAmet\n"

/**
 * Test for [SpaceTokenizer]
 */
class TokenizerTest {
    @Test
    fun `default tokenizer splits text by whitespaces`() {
        assertContentEquals(sequenceOf("Lorem", "Ipsum", "Dolor", "Sit", "Amet"), SpaceTokenizer().split(TEXT))
    }

    @Test
    fun `sequence is empty of empty class`() {
        assertContentEquals(sequenceOf(), SpaceTokenizer().split(""))
    }
}