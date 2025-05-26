package com.vopenia.livekit.participant.track.local

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual class LocalNoneTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : LocalTrack(scope, track) {
    // nothing for now
}
