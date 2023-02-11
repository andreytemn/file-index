package com.github.andreytemn.fileindex

import kotlinx.coroutines.*
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchService
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Serves the file index requests. Read and write operations are synchronized with different locks to maintain the invariant for the index.
 * The service watches the file system events of the given path. The watcher works in the provided scope.
 */
internal class FileIndexService(
    private val path: File,
    private val fileIndexStorage: FileIndexStorage,
    private val fileLoadingDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AutoCloseable {

    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
    private lateinit var watchService: WatchService
    private var initialized: Boolean = false
    private lateinit var watchCoroutine: Job

    fun initialize(scope: CoroutineScope) {
        if (initialized) throw IllegalStateException("Already initialized")
        watchCoroutine = scope.launch {
            watchPath(path, scope)
        }
        initIndex(path)
        initialized = true
    }

    private suspend fun watchPath(
        path: File, scope: CoroutineScope
    ) {
        watchService = FileSystems.getDefault().newWatchService()
        path.toPath().register(
            watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE
        )
        while (true) {
            val key = watchService.take()
            watchService.take()
            for (event in key.pollEvents()) {
                when (event.kind()) {
                    ENTRY_CREATE -> addSingleFile((event.context() as Path).toFile(), scope)
                    ENTRY_MODIFY, ENTRY_DELETE -> updateIndex(path, scope)
                }
            }
            if (!key.reset()) {
                break
            }
        }
    }

    /**
     * Get a sequence of files that contain the [word]. May block if the index is being updated.
     */
    operator fun get(word: String): Sequence<File> = lock.read { fileIndexStorage[word] }

    /**
     * Stop watching the path.
     */
    override fun close() {
        if (!initialized) throw IllegalStateException("Not initialized or already closed")
        if (this::watchCoroutine.isInitialized) watchCoroutine.cancel("Watcher closed")
        initialized = false
    }

    private suspend fun addSingleFile(file: File, scope: CoroutineScope) {
        if (file.isFile) lock.write { loadFile(file, scope) }
    }

    private suspend fun updateIndex(path: File, scope: CoroutineScope) {
        lock.write {
            fileIndexStorage.clear()
            collectIndex(path, scope)
        }
    }

    private suspend fun loadFile(file: File, scope: CoroutineScope) =
        withContext(fileLoadingDispatcher + CoroutineName("Loading file ${file.name}")) {
            loadFile(file)
        }


    private suspend fun collectIndex(path: File, scope: CoroutineScope) {
        if (path.isDirectory) path.listFiles()?.forEach { collectIndex(it, scope) } else loadFile(path, scope)
    }

    private fun initIndex(path: File) {
        lock.write { collectIndex(path) }
    }

    private fun collectIndex(path: File) {
        if (path.isDirectory) path.listFiles()?.forEach { collectIndex(it) } else loadFile(path)
    }

    private fun loadFile(file: File) = fileIndexStorage.add(file)
}