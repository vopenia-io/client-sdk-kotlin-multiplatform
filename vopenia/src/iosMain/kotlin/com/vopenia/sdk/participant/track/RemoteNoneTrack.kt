package com.vopenia.sdk.participant.track

import LiveKitClient.RemoteTrackPublication
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual class RemoteNoneTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track) {
    // nothing for now
}
