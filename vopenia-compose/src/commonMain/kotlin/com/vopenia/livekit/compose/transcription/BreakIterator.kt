package com.vopenia.livekit.compose.transcription

class BreakIterator(
    text: String
) {
    private val tokens = text.split(" ")
    private var index = 0

    fun next() = tokens.getOrNull(index).also { index++ }
}