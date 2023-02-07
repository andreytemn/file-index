package com.github.andreytemn.fileindex

import java.io.File
import kotlin.io.path.createTempDirectory

const val FILE1 = "file1.txt"
const val FILE2 = "file2.txt"
const val FILE3 = "file3.txt"
const val FILE4 = "nested\\file4.txt"

/**
 * Serves as a holder for the [ClassLoader] to load the resources
 */
private object ResourceAnchor

/**
 * Get a list of files from test resources with given [names]
 */
fun getFiles(vararg names: String): List<File> = names.map {
    getFile(it)
}

/**
 * Get a file from test resources with given [name]
 */
fun getFile(name: String) = File(ResourceAnchor::class.java.getResource(name)!!.toURI())

/**
 * Get a parent directory of the files to test
 */
fun getResourceDir(): File = getFile(FILE1).parentFile

/**
 * Create a file with given [name] in the [dir] that will auto-delete on exit.
 */
fun createFile(dir: File, name: String): File {
    val file = File(dir, name)
    file.deleteOnExit()
    file.createNewFile()
    return file
}

/**
 * Create a temp folder that will auto-delete on exit.
 */
fun createTempDir(): File = createTempDirectory("file-index").toFile().also { it.deleteOnExit() }