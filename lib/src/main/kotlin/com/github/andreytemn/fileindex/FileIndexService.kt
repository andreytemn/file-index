package com.github.andreytemn.fileindex

import dev.vishna.watchservice.KWatchEvent
import dev.vishna.watchservice.KWatchEvent.Kind.*
import dev.vishna.watchservice.asWatchChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Serves the file index requests. Read and write operations are synchronized with different locks to maintain the invariant for the index.
 * The service watches the file system events of the given path. THe watcher works in the provided scope.
 */
internal class FileIndexService(
    scope: CoroutineScope,
    path: File,
    private val fileIndex: FileIndex
) : AutoCloseable {

    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
    private val watchChannel: Channel<KWatchEvent>

    init {
        watchChannel = path.asWatchChannel()
        watch(scope, path)
    }

    /**
     * Get a sequence of files that contain the [word]. May block if the index is being updated.
     */
    operator fun get(word: String): Sequence<File> =
        lock.read { fileIndex[word] }

    /**
     * Stop watching the path.
     */
    override fun close() {
        watchChannel.close()
    }

    private fun watch(scope: CoroutineScope, path: File) {
        scope.launch {
            for (event in watchChannel) {
                when (event.kind) {
                    Initialized -> initIndex(path)
                    Created -> addSingleFile(event.file)
                    Deleted, Modified -> updateIndex(path)
                }
            }
        }
    }

    private fun addSingleFile(file: File) {
        if (file.isFile) lock.write { fileIndex.add(file) }
    }

    private fun updateIndex(path: File) {
        lock.write {
            fileIndex.clear()
            collectIndex(path)
        }
    }

    private fun initIndex(path: File) {
        lock.write { collectIndex(path) }
    }

    private fun collectIndex(path: File) {
        if (path.isDirectory)
            path.listFiles()?.forEach { collectIndex(it) }
        else
            fileIndex.add(path)
    }
}