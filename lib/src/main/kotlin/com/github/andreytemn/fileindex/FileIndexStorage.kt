package com.github.andreytemn.fileindex

import java.io.File

/**
 * Stores the reversed file index with mapping from words to the files that contain them.
 */
interface FileIndexStorage {
    /**
     * Add the [file] to the index
     */
    fun add(file: File)

    /**
     * Remove all the stored files
     */
    fun clear()

    /**
     * Return a [Sequence] of files that contain the given [word] or empty if none found
     */
    operator fun get(word: String): Sequence<File>
}