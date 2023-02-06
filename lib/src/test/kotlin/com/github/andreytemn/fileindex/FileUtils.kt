package com.github.andreytemn.fileindex

import java.io.File

const val FILE1 = "file1.txt"
const val FILE2 = "file2.txt"
const val FILE3 = "file3.txt"
const val FILE4 = "nested\\file4.txt"

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
 * Serves as a holder for the [ClassLoader] to load the resources
 */
private object ResourceAnchor