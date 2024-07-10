package com.vopenia.sdk.participant.track

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InternalRemoteTrack : RemoteTrack {
    private val remoteTrackState = MutableStateFlow(RemoteTrackState())
    override val state = remoteTrackState.asStateFlow()

    override val name = "NOTHING"

    override val isEnabled = false

    override val isSubscriptionAllowed = false

    override val kind = Kind.None

    override val sid = ""

    override fun addRenderer(videoSink: VideoSink) {
        TODO("Not yet implemented")
    }

    override fun removeRenderer(videoSink: VideoSink) {
        TODO("Not yet implemented")
    }

    override suspend fun enable(enable: Boolean) {
        TODO("NotImplemented")
    }

    override suspend fun subscribe(subscribe: Boolean) {
        TODO("NotImplemented")
    }

    internal fun setSubscribed(subscribed: Boolean) {
        TODO("NotImplemented")
    }

    internal fun setPublished(published: Boolean) {
        TODO("NotImplemented")
    }
}
