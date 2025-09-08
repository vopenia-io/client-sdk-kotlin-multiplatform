package com.vopenia.livekit.participant.track

import io.livekit.android.room.types.TranscriptionSegment

fun TranscriptionSegment.toLocalTranscriptionSegment() =
    com.vopenia.livekit.participant.transcription.TranscriptionSegment(
        id = this.id,
        transient = !this.final,
        text = this.text
    )