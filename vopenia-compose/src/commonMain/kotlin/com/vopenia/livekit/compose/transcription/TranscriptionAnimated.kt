package com.vopenia.livekit.compose.transcription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun TranscriptionAnimated(
    modifier: Modifier,
    text: String,
    typingDelayInMs: Long = 50L,
    content: @Composable (Modifier, String) -> Unit
) {
    var lastUpdated by remember { mutableStateOf(-1) }

    var substringText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        // Initial start delay of the typing animation
        val iterator = BreakIterator(text)
        substringText = ""
        delay(typingDelayInMs)

        var lastChar: String? = ""
        val savedLastUpdated = lastUpdated + 1
        lastUpdated = savedLastUpdated

        do {
            if (lastUpdated != savedLastUpdated) return@LaunchedEffect

            lastChar = iterator.next()

            lastChar?.let {
                substringText = "$substringText $it"
                delay(typingDelayInMs)
            }
        } while (null != lastChar && lastUpdated == savedLastUpdated)
    }

    content(modifier, substringText)
}