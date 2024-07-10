package com.vopenia.sdk.participant.track

import kotlinx.coroutines.flow.StateFlow

interface RemoteTrack {
    val state: StateFlow<RemoteTrackState>

    val name: String

    val isEnabled: Boolean

    val isSubscriptionAllowed: Boolean

    val kind: Kind

    val sid: String

    fun addRenderer(videoSink: VideoSink)

    fun removeRenderer(videoSink: VideoSink)

    suspend fun enable(enable: Boolean)

    suspend fun subscribe(subscribe: Boolean)
}
