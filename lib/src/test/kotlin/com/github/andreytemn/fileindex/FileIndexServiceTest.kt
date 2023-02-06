package com.github.andreytemn.fileindex

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertContentEquals

/**
 * Test for [FileIndexService]
 */
class FileIndexServiceTest {

    @Test(expected = CancellationException::class)
    fun `initialization reads all files`() =
        runBlocking {
            val file = getFile(FILE1)
            val service = FileIndexService(this, file.parentFile, ConcurrentUpdateFileIndex(SpaceTokenizer()))
            delay(1000)
            assertContentEquals(getFiles(FILE4, FILE1, FILE2, FILE3).asSequence(), service["sit"])
            cancel()
        }
}
