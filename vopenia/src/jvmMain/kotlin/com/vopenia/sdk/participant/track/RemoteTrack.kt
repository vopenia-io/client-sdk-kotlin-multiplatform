package com.vopenia.sdk.participant.track

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual sealed class RemoteTrack : SubRemoteTrack(
    CoroutineScope(
        Dispatchers.Main
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

    }
}
