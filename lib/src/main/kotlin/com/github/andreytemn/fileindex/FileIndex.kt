package com.github.andreytemn.fileindex

import kotlinx.coroutines.CoroutineScope
import java.io.File

/**
 * A reversed file index that stores a mapping from string words to the files that contain them.
 * The FileIndex watches for the changes in the path and maintains the consistency of the index.
 * It is concurrent and allows parallel reading of files. Querying files is blocking and waits for the latest index
 * update to finish in order to always return actual data.
 *
 * FileIndex accepts a custom [Tokenizer] for splitting the text from files into tokens.
 * The default [SpaceTokenizer] splits the text by whitespaces
 *
 * @param scope the scope to launch file watcher and load files
 * @param path the directory to build the index
 * @param tokenizer the tokenizer to split the file content
 *
 * @author Andrei Temnikov
 */
class FileIndex(
    scope: CoroutineScope,
    path: File,
    tokenizer: Tokenizer = SpaceTokenizer()
) : AutoCloseable {
    private val service: FileIndexService

    init {
        service = FileIndexService(scope, path, ConcurrentUpdateFileIndexStorage(tokenizer))
    }

    operator fun get(word: String) = service[word]
    override fun close() {
        service.close()
    }
}