package com.github.andreytemn.fileindex

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertContentEquals

/**
 * Test for [FileIndexService]
 */
class FileIndexServiceTest {

    @Test(expected = CancellationException::class)
    fun `initialization reads all files`() = runBlocking {
        val service = FileIndexService(this, getResourceDir(), ConcurrentUpdateFileIndex(SpaceTokenizer()))
        delay(1000)

        assertContentEquals(getFiles(FILE4, FILE1, FILE2, FILE3).asSequence(), service["sit"])

        service.close()
        cancel()
    }

    @Test(expected = CancellationException::class)
    fun `adding single files does not invalidate cache`() = runBlocking {
        val index = mock<FileIndex>()
        val dir = createTempDir()
        val service = FileIndexService(this, dir, index)

        val file = createFile(dir, "name")
        delay(1000)

        verify(index).add(file)
        verify(index, times(0)).clear()

        service.close()
        cancel()
    }

    @Test(expected = CancellationException::class)
    fun `file deleting triggers cache invalidation`() = runBlocking {
        val index = mock<FileIndex>()
        val dir = createTempDir()
        val service = FileIndexService(this, dir, index)

        val file = createFile(dir, "name")
        delay(1000)
        verify(index).add(file)

        file.delete()
        delay(1000)
        verify(index, atLeastOnce()).clear()

        service.close()
        cancel()
    }


    @Test(expected = CancellationException::class)
    fun `file modification rebuilds cache`() = runBlocking {
        val dir = createTempDir()
        val service = FileIndexService(this, dir, ConcurrentUpdateFileIndex(SpaceTokenizer()))

        val file = createFile(dir, "name")
        file.writeText("text1\ntext2")
        delay(1000)

        assertContentEquals(sequenceOf(file), service["text2"])
        assertContentEquals(sequenceOf(), service["text3"])

        file.writeText("text3")
        delay(1000)

        assertContentEquals(sequenceOf(file), service["text3"])
        assertContentEquals(sequenceOf(), service["text2"])

        service.close()
        cancel()
    }
}
