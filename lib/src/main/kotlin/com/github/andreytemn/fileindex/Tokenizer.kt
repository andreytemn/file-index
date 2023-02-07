package com.github.andreytemn.fileindex

/**
 * Splits the file text content into tokens, optionally applying filtration.
 */
interface Tokenizer {
    /**
     * Split the given [text] into a sequence of tokens.
     */
    fun split(text: String): Sequence<String>

    /**
     * Filter the [word] from the index. Return true if the word should present in the index.
     */
    fun filter(word: String): Boolean = true
}

/**
 * Default tokenizer that splits a line into words by whitespaces.
 */
class SpaceTokenizer : Tokenizer {
    override fun split(text: String): Sequence<String> =
        text.split("\\s+".toRegex()).asSequence().filter { it.isNotEmpty() }
}