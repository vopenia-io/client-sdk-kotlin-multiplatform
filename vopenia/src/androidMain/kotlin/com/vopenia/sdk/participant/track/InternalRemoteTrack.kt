package com.vopenia.sdk.participant.track

import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InternalRemoteTrack(
    private val scope: CoroutineScope,
    val track: RemoteTrackPublication
) : RemoteTrack {
    private val remoteTrackState = MutableStateFlow(RemoteTrackState())
    override val state = remoteTrackState.asStateFlow()

    override val kind = kindFrom(track.kind)
    override val sid = track.sid

    override val name = track.name

    override val isEnabled = track.isAutoManaged //?

    override val isSubscriptionAllowed = track.subscriptionAllowed

    override fun addRenderer(videoSink: VideoSink) {
        track.track?.let {
            if (it is VideoTrack) {
                it.addRenderer(videoSink)
            }
        }
    }

    override fun removeRenderer(videoSink: VideoSink) {
        track.track?.let {
            if (it is VideoTrack) {
                it.removeRenderer(videoSink)
            }
        }
    }

    override suspend fun enable(enable: Boolean) {
        track.setEnabled(enable)
    }

    override suspend fun subscribe(subscribe: Boolean) {
        track.setSubscribed(subscribe)
    }

    internal fun setSubscribed(subscribed: Boolean) {
        scope.launch {
            remoteTrackState.emit(remoteTrackState.value.copy(subscribed = subscribed))
        }
    }

    internal fun setPublished(published: Boolean) {
        scope.launch {
            remoteTrackState.emit(remoteTrackState.value.copy(published = published))
        }
    }

    val nativeTrack
        get() = track.track
}

fun kindFrom(value: Track.Kind) = when (value) {
    Track.Kind.AUDIO -> Kind.Audio
    Track.Kind.VIDEO -> Kind.Video
    Track.Kind.UNRECOGNIZED -> Kind.None
}