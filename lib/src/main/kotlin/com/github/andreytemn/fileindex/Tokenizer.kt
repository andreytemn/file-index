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
     * Remove not required tokens from the given sequence of [tokens]. By default, filtration is not applied.
     */
    fun filter(tokens: Sequence<String>): Sequence<String> = tokens
}

class SpaceTokenizer : Tokenizer {
    override fun split(text: String): Sequence<String> = text.split("\\s+".toRegex()).asSequence().filter { it.isNotEmpty() }
}