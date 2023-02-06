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
}