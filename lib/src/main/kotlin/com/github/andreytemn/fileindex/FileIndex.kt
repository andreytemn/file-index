package com.github.andreytemn.fileindex

import dev.vishna.watchservice.KWatchEvent
import dev.vishna.watchservice.asWatchChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class FileIndex(
    scope: CoroutineScope,
    path: File,
    private val index: MutableMap<String, List<File>> = ConcurrentHashMap()
) : AutoCloseable {

    init {
        watch(scope, path)
    }

    private lateinit var watchChannel: Channel<KWatchEvent>
    private fun watch(scope: CoroutineScope, path: File) {
        watchChannel = path.asWatchChannel()

        scope.launch {
            for (event in watchChannel) {
                when (event.kind) {
                    KWatchEvent.Kind.Created, KWatchEvent.Kind.Deleted, KWatchEvent.Kind.Modified -> updateCache(path)
                    KWatchEvent.Kind.Initialized -> collectIndex(path)
                }
            }
        }
    }

    private fun collectIndex(path: File) {

    }

    private fun updateCache(path: File) {
        index.clear()
        collectIndex(path)
    }

    override fun close() {
        watchChannel.close()
    }
}