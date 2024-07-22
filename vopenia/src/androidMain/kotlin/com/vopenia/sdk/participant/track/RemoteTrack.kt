package com.vopenia.sdk.participant.track

import io.livekit.android.room.track.Track
import kotlinx.coroutines.CoroutineScope

actual sealed class RemoteTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : com.vopenia.sdk.participant.track.Track(scope, trackStateFromPublication(track)) {
    private var internalTrack: RemoteTrackPublication = track

    actual val track: RemoteTrackPublication
        get() = internalTrack

    actual override val kind = kindFrom(track.kind)
    actual override val sid = track.sid

    actual override val name = track.name

    actual override val source = track.source.toSource()

    actual val isEnabled = track.isAutoManaged // ?

    actual val isSubscriptionAllowed = track.subscriptionAllowed

    internal actual fun updateInternalTrack(track: RemoteTrackPublication) {
        internalTrack = track
    }

    actual suspend fun enable(enable: Boolean) {
        track.setEnabled(enable)
    }

    actual suspend fun subscribe(subscribe: Boolean) {
        track.setSubscribed(subscribe)
    }
}

fun kindFrom(value: Track.Kind) = when (value) {
    Track.Kind.AUDIO -> Kind.Audio
    Track.Kind.VIDEO -> Kind.Video
    Track.Kind.UNRECOGNIZED -> Kind.None
}
