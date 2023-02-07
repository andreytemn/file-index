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
        val tokenizer = SpaceTokenizer()
        assertContentEquals(
            sequenceOf("Lorem", "Ipsum", "Dolor", "Sit", "Amet"),
            tokenizer.split(TEXT).filter { tokenizer.filter(it) }.map { tokenizer.map(it) })
    }

    @Test
    fun `sequence is empty of empty class`() {
        assertContentEquals(sequenceOf(), SpaceTokenizer().split(""))
    }
}