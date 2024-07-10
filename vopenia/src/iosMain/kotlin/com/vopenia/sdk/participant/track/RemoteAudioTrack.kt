package com.vopenia.sdk.participant.track

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual class RemoteAudioTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track) {
    // nothing for now
}
