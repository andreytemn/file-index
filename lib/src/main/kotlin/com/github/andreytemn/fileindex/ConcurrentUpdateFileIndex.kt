package com.github.andreytemn.fileindex

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * A [FileIndex] implementation that allows concurrent adding new files.
 * Updating and writing the index should be synchronized externally to maintain its consistency.
 * The tokenizer is required to split the file content into words.
 */
class ConcurrentUpdateFileIndex(
    private val tokenizer: Tokenizer,
    private val storage: MutableMap<String, MutableSet<File>> = ConcurrentHashMap()
) : FileIndex {
    override fun add(file: File) {
        tokenizer.split(file.readText()).forEach { storage.getOrPut(it) { hashSetOf() }.add(file) }
    }

    override fun clear() {
        storage.clear()
    }

    override operator fun get(word: String) = storage[word]?.asSequence() ?: sequenceOf()
}