package com.github.andreytemn.fileindex

import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

/**
 * Test for [ConcurrentUpdateFileIndex]
 */
class FileIndexTest {
    @Test
    fun `querying word that presents in multiple files returns all of them`() {
        val index = ConcurrentUpdateFileIndex(SpaceTokenizer())
        val files = getFiles(FILE1, FILE2, FILE3)

        files.forEach(index::add)

        assertContentEquals(files.asSequence(), index["sit"])
    }

    @Test
    fun `cleared index is empty`() {
        val index = ConcurrentUpdateFileIndex(SpaceTokenizer())

        getFiles(FILE1).forEach(index::add)

        assertContentEquals(sequenceOf(getFile(FILE1)), index["sit"])

        index.clear()

        assertTrue { index["sit"].toList().isEmpty() }
    }

    @Test
    fun `querying non-existing word returns empty sequence`() {
        val index = ConcurrentUpdateFileIndex(SpaceTokenizer())
        getFiles(FILE1, FILE2, FILE3).forEach(index::add)

        assertContentEquals(sequenceOf(), index["non-existing"])
    }

    @Test
    fun `custom tokenizer filtrates and maps tokens`() {
        val index = ConcurrentUpdateFileIndex(object : Tokenizer {
            override fun split(text: String) = text.split("").asSequence()
            override fun map(word: String) = word.uppercase()
            override fun filter(word: String) = word == "b"
        })
        getFiles(FILE1, FILE2, FILE3).forEach(index::add)

        assertContentEquals(sequenceOf(getFile(FILE2)), index["B"])
        assertContentEquals(sequenceOf(), index["b"])
    }

    @Test
    fun `file that contains a word several times presents only once`() {
        val index = ConcurrentUpdateFileIndex(SpaceTokenizer())
        val file = getFile(FILE4)

        index.add(file)

        assertContentEquals(sequenceOf(file), index["sit"])
    }
}