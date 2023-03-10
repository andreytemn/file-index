package com.github.andreytemn.fileindex

import org.junit.Test
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

/**
 * Test for [ConcurrentUpdateFileIndexStorage]
 */
class FileIndexStorageTest {
    @Test
    fun `querying word that presents in multiple files returns all of them`() {
        val index = ConcurrentUpdateFileIndexStorage(SpaceTokenizer())
        val files = getFiles(FILE1, FILE2, FILE3)

        files.forEach(index::add)

        assertContentEquals(files.asSequence(), index["sit"])
    }

    @Test
    fun `cleared index is empty`() {
        val index = ConcurrentUpdateFileIndexStorage(SpaceTokenizer())

        getFiles(FILE1).forEach(index::add)

        assertContentEquals(sequenceOf(getFile(FILE1)), index["sit"])

        index.clear()

        assertTrue { index["sit"].toList().isEmpty() }
    }

    @Test
    fun `querying non-existing word returns empty sequence`() {
        val index = ConcurrentUpdateFileIndexStorage(SpaceTokenizer())
        getFiles(FILE1, FILE2, FILE3).forEach(index::add)

        assertContentEquals(sequenceOf(), index["non-existing"])
    }

    @Test
    fun `custom tokenizer filtrates and maps tokens`() {
        val index = ConcurrentUpdateFileIndexStorage(object : Tokenizer {
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
        val index = ConcurrentUpdateFileIndexStorage(SpaceTokenizer())
        val file = getFile(FILE4)

        index.add(file)

        assertContentEquals(sequenceOf(file), index["sit"])
    }

    @Test
    fun `no exceptions thrown on non-existing file`(){
        val index = ConcurrentUpdateFileIndexStorage(SpaceTokenizer())
        val file = File("non-existing")

        index.add(file)
    }
}