package com.vopenia.sdk.participant.track

import com.vopenia.sdk.utils.Dispatchers
import kotlinx.coroutines.CoroutineScope

actual sealed class RemoteTrack : SubRemoteTrack(
    CoroutineScope(
        Dispatchers.Default
    )
) {
    actual val track: RemoteTrackPublication = "NOTHING"

    actual val name = "NOTHING"

    actual val isEnabled = false

    actual val isSubscriptionAllowed = false

    actual val kind = Kind.None

    actual val sid = ""

    actual suspend fun enable(enable: Boolean) {
        TODO("NotImplemented")
    }

    actual suspend fun subscribe(subscribe: Boolean) {
        TODO("NotImplemented")
    }

    internal actual fun updateInternalTrack(track: RemoteTrackPublication) {
        // nothing
    }
}
