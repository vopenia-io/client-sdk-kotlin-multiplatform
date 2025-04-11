package com.vopenia.sdk.participant.track

import LiveKitClient.TrackKind
import LiveKitClient.TrackSource
import com.vopenia.sdk.NSErrorException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual sealed class RemoteTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : Track(scope, trackStateFromPublication(track)) {
    private var internalTrack = track

    actual val track: RemoteTrackPublication
        get() = internalTrack

    actual override val name = track.name()

    actual val isEnabled = track.isEnabled()

    actual val isSubscriptionAllowed = track.isSubscriptionAllowed()
    actual override val kind = kindFrom(track.kind())
    actual override val sid = track.sid().stringValue()

    actual override val source: Source = sourceFrom(track.source())

    actual suspend fun enable(enable: Boolean) {
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

    actual suspend fun subscribe(subscribe: Boolean) {
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

    internal actual fun updateInternalTrack(track: RemoteTrackPublication) {
        internalTrack = track
    }
}

fun kindFrom(value: TrackKind) = when (value) {
    0L -> Kind.Audio
    1L -> Kind.Video
    else -> Kind.None
}

@Suppress("MagicNumber")
fun sourceFrom(value: TrackSource) = when (value) {
    0L -> Source.UNKNOWN // case unknown
    1L -> Source.CAMERA // case camera
    2L -> Source.MICROPHONE // case microphone
    3L -> Source.SCREEN_SHARE // case screenShareVideo
    4L -> Source.SCREEN_SHARE // case screenShareAudio
    else -> Source.UNKNOWN
}
