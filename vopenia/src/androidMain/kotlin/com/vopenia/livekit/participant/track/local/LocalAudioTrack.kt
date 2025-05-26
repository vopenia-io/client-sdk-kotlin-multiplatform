package com.vopenia.livekit.participant.track.local

import kotlinx.coroutines.CoroutineScope

actual class LocalAudioTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : LocalTrack(scope, track) {
    // nothing for now
}
