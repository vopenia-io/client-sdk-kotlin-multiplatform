package com.vopenia.livekit.participant.track.local

data class InternalLocalTrackPublication(
    val value: String
)

actual typealias LocalTrackPublication = InternalLocalTrackPublication
