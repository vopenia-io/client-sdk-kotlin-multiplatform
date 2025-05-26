package com.vopenia.livekit.participant.track

data class InternalRemoteTrackPublication(
    val value: String
)

actual typealias RemoteTrackPublication = InternalRemoteTrackPublication
