package com.vopenia.livekit.participant.transcription

data class TranscriptionSegment(
    val transient: Boolean,
    val text: String,
    val id: String
)