package com.vopenia.sdk.participant.track

data class RemoteTrackState(
    val subscribed: Boolean = false,
    val published: Boolean = false,
    val active: Boolean = false
)
