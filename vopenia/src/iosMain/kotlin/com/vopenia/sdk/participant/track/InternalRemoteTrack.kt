package com.vopenia.sdk.participant.track

import LiveKitClient.RemoteTrackPublication
import LiveKitClient.RemoteVideoTrack
import LiveKitClient.TrackKind
import com.vopenia.sdk.NSErrorException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class InternalRemoteTrack(
    private val scope: CoroutineScope,
    val track: RemoteTrackPublication
) : RemoteTrack {
    private val remoteTrackState = MutableStateFlow(RemoteTrackState())
    override val state = remoteTrackState.asStateFlow()

    override val name = track.name()

    override val isEnabled = track.isEnabled()

    override val isSubscriptionAllowed = track.isSubscriptionAllowed()
    override val kind = kindFrom(track.kind())
    override val sid = track.sid().stringValue()

    override fun addRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is RemoteVideoTrack) {
                videoSink.videoView.setTrack(it)
            }
        }
    }

    override fun removeRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is RemoteVideoTrack) {
                videoSink.videoView.setTrack(null)
            }
        }
    }

    override suspend fun enable(enable: Boolean) {
        suspendCoroutine { continuation ->
            track.setWithEnabled(enable) {
                if (null != it) {
                    continuation.resumeWithException(NSErrorException(it))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    override suspend fun subscribe(subscribe: Boolean) {
        suspendCoroutine { continuation ->
            track.setWithSubscribed(subscribe) {
                if (null != it) {
                    continuation.resumeWithException(NSErrorException(it))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
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
        get() = track.track()
}

fun kindFrom(value: TrackKind) = when (value) {
    0L -> Kind.Audio
    1L -> Kind.Video
    else -> Kind.None
}