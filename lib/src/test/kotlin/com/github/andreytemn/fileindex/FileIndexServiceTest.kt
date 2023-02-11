package com.github.andreytemn.fileindex

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertContentEquals

/**
 * Test for [FileIndexService]
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FileIndexServiceTest {

    @Test
    fun `initialization reads all files`() = runTest {
        val service = FileIndexService(getResourceDir(), ConcurrentUpdateFileIndexStorage(SpaceTokenizer()))
        service.initialize(this)

        assertContentEquals(getFiles(FILE4, FILE1, FILE2, FILE3).asSequence().sorted(), service["sit"].sorted())

        service.close()
    }

    @Test
    fun `adding single files does not invalidate cache`() = runTest {
        val index = mock<FileIndexStorage>()
        val dir = createTempDir()
        val service = FileIndexService(dir, index)
        launch {
            service.initialize(this)
            val file = createFile(dir, "name")
            advanceUntilIdle()
            verify(index, timeout(1000)).add(file)
            verify(index, times(0)).clear()
            service.close()
        }.cancel()
    }

    @Test
    fun `file deleting triggers cache invalidation`() = runTest {
        val index = mock<FileIndexStorage>()
        val dir = createTempDir()
        launch {
            val service = FileIndexService(
                dir, index,
                StandardTestDispatcher(testScheduler)
            )

            service.initialize(this)
            advanceUntilIdle()

            val file = createFile(dir, "name")
            advanceUntilIdle()
            verify(index).add(file)

            file.delete()
            advanceUntilIdle()
            verify(index, atLeastOnce()).clear()

            service.close()
        }.cancel()
    }


    @Test
    fun `file modification rebuilds cache`() = runTest {
        val dir = createTempDir()
        val service = FileIndexService(
            dir, ConcurrentUpdateFileIndexStorage(SpaceTokenizer()),
            StandardTestDispatcher(testScheduler)
        )
        launch {
            service.initialize(this)
            advanceUntilIdle()

            val file = createFile(dir, "name")
            file.writeText("text1\ntext2")
            advanceUntilIdle()

            assertContentEquals(sequenceOf(file), service["text2"])
            assertContentEquals(sequenceOf(), service["text3"])

            file.writeText("text3")
            advanceUntilIdle()

            assertContentEquals(sequenceOf(file), service["text3"])
            assertContentEquals(sequenceOf(), service["text2"])

            service.close()
        }.cancel()
    }
}
