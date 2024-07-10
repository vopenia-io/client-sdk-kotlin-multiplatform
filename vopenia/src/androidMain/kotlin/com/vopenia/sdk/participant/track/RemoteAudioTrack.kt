package com.vopenia.sdk.participant.track

import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope

actual class RemoteAudioTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track) {
    // nothing for now
}
