package com.github.andreytemn.fileindex

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertContentEquals

/**
 * Test for [FileIndexService]
 */

@OptIn(ExperimentalCoroutinesApi::class)
class FileIndexServiceTest {

    @Test
    fun `initialization reads all files`() = runTest {
        val path = getResourceDir()
        val tokenizer = SpaceTokenizer()
        val fileIndexStorage = ConcurrentUpdateFileIndexStorage(tokenizer)

        val service = FileIndexService(this, path, fileIndexStorage)
        delay()

        assertContentEquals(getFiles(FILE4, FILE1, FILE2, FILE3).asSequence(), service["sit"])

        service.close()
    }

    @Test
    fun `adding single files does not invalidate cache`() = runTest {
        val index = mock<FileIndexStorage>()
        val dir = createTempDir()
        val service = FileIndexService(this, dir, index)

        val file = createFile(dir, "name")
        delay()

        verify(index).add(file)
        verify(index, times(0)).clear()

        service.close()
    }

    @Test
    fun `file deleting triggers cache invalidation`() = runTest {
        val index = mock<FileIndexStorage>()
        val dir = createTempDir()
        val service = FileIndexService(this, dir, index)

        val file = createFile(dir, "name")
        delay()
        verify(index).add(file)

        file.delete()
        delay()
        verify(index, atLeastOnce()).clear()

        service.close()
    }


    @Test
    fun `file modification rebuilds cache`() = runTest {
        val dir = createTempDir()
        val service = FileIndexService(this, dir, ConcurrentUpdateFileIndexStorage(SpaceTokenizer()))

        val file = createFile(dir, "name")
        file.writeText("text1\ntext2")
        delay()

        assertContentEquals(sequenceOf(file), service["text2"])
        assertContentEquals(sequenceOf(), service["text3"])

        file.writeText("text3")
        delay()

        assertContentEquals(sequenceOf(file), service["text3"])
        assertContentEquals(sequenceOf(), service["text2"])

        service.close()
    }
}
