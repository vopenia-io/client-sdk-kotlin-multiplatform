package com.vopenia.livekit.participant.track

import com.vopenia.sdk.utils.Dispatchers
import kotlinx.coroutines.CoroutineScope

actual sealed class RemoteTrack : Track(
    CoroutineScope(
        Dispatchers.Default
    ),
    TrackState()
) {
    actual val track = InternalRemoteTrackPublication("NOTHING")

    actual override val name = "NOTHING"

    actual val isEnabled = false

    actual val isSubscriptionAllowed = false

    actual override val kind = Kind.None

    actual override val sid = ""

    actual suspend fun enable(enable: Boolean) {
        TODO("NotImplemented")
    }

    actual suspend fun subscribe(subscribe: Boolean) {
        TODO("NotImplemented")
    }

    internal actual fun updateInternalTrack(track: RemoteTrackPublication) {
        // nothing
    }

    actual override val source = Source.UNKNOWN
}
