package com.vopenia.sdk.participant.track

data class InternalRemoteTrackPublication(
    val value: String
)

actual typealias RemoteTrackPublication = InternalRemoteTrackPublication
