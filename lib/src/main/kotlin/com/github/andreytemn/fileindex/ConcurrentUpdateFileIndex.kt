package com.github.andreytemn.fileindex

import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * A [FileIndex] implementation that allows concurrent adding new files.
 * Updating and writing the index should be synchronized externally to maintain its consistency.
 * The tokenizer is required to split the file content into words.
 */
internal class ConcurrentUpdateFileIndex(
    private val tokenizer: Tokenizer
) : FileIndex {

    private val storage: MutableMap<String, MutableSet<File>> = ConcurrentHashMap()
    override fun add(file: File) {
        file.bufferedReader().useLines {
            it.flatMap(tokenizer::split).forEach { str -> put(str, file) }
        }
    }

    private fun put(str: String, file: File) {
        storage.getOrPut(str) { hashSetOf() }.add(file)
    }

    override fun clear() {
        storage.clear()
    }

    override operator fun get(word: String) = storage[word]?.asSequence() ?: sequenceOf()
}