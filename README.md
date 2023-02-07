# File Index

A reversed file index that stores a mapping from string words to the files that contain them.
The FileIndex watches for the changes in the directory and maintains the consistency of the index.
It is concurrent and allows parallel reading of files.
Querying files is blocking and waits for the latest index update to finish in order to always return actual data.

**FileIndex** accepts a custom **Tokenizer** for splitting the text from files into tokens.
The default **SpaceTokenizer** splits the text by whitespaces

## Installation

```
git clone https://github.com/andreytemn/file-index.git

cd file-index

./gradlew publishToMavenLocal
```

## Gradle Build Script

```
repositories {
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.andreytemn:file-index:1.0.0")
}
```

## Usage example

```
launch {
    val tokenizer = object : Tokenizer {
        override fun split(text: String): Sequence<String> =
            text.split(" +".toRegex()).filter { it.isEmpty() }.asSequence()

        override fun map(word: String): String = word.uppercase()
        override fun filter(word: String): Boolean = word.length >= 5
    }
    FileIndex(this, directory, tokenizer).use { println(it["HELLO"]) }
}
```

## Legal Note

MIT License

Copyright (c) 2023 andreytemn