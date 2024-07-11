package com.vopenia.sdk.participant.track

import io.livekit.android.room.track.RemoteTrackPublication
import kotlinx.coroutines.CoroutineScope

actual class RemoteNoneTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track) {
    // nothing for now
}
