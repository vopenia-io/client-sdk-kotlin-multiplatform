package com.vopenia.sdk.participant.track

import LiveKitClient.TrackKind
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
) : SubRemoteTrack(scope) {
    private var internalTrack = track

    actual val track: RemoteTrackPublication
        get() = internalTrack

    actual val name = track.name()

    actual val isEnabled = track.isEnabled()

    actual val isSubscriptionAllowed = track.isSubscriptionAllowed()
    actual val kind = kindFrom(track.kind())
    actual val sid = track.sid().stringValue()

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
