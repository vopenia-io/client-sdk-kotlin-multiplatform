package com.vopenia.sdk.participant.track.local

data class InternalLocalTrackPublication(
    val value: String
)

actual typealias LocalTrackPublication = InternalLocalTrackPublication
