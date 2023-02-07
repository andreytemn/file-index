package com.github.andreytemn.fileindex

import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap

/**
 * A [FileIndexStorage] implementation that allows concurrent adding new files.
 * Updating and writing the index should be synchronized externally to maintain its consistency.
 * The tokenizer is required to split the file content into words.
 */
internal class ConcurrentUpdateFileIndexStorage(
    private val tokenizer: Tokenizer,
    private val charset: Charset = Charsets.UTF_8
) : FileIndexStorage {

    private val storage: MutableMap<String, MutableSet<File>> = ConcurrentHashMap()
    override fun add(file: File) {
        if (file.exists() && file.canRead())
            file.bufferedReader(charset).useLines { seq ->
                seq.flatMap(tokenizer::split).filter { tokenizer.filter(it) }.map { tokenizer.map(it) }
                    .forEach { put(it, file) }
            }
    }

    override fun clear() {
        storage.clear()
    }

    override operator fun get(word: String) = storage[word]?.asSequence() ?: sequenceOf()

    private fun put(str: String, file: File) {
        storage.getOrPut(str) { hashSetOf() }.add(file)
    }
}