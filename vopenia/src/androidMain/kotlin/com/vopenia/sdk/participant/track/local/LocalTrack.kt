package com.vopenia.sdk.participant.track.local

import com.vopenia.sdk.participant.track.kindFrom
import com.vopenia.sdk.participant.track.toSource
import com.vopenia.sdk.participant.track.trackStateFromPublication
import kotlinx.coroutines.CoroutineScope

actual sealed class LocalTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : com.vopenia.sdk.participant.track.Track(scope, trackStateFromPublication(track)) {
    private var internalTrack: LocalTrackPublication = track

    actual val track: LocalTrackPublication
        get() = internalTrack

    actual override val kind = kindFrom(track.kind)
    actual override val sid = track.sid

    actual override val name = track.name

    actual override val source = track.source.toSource()

    internal actual fun updateInternalTrack(track: LocalTrackPublication) {
        internalTrack = track
    }
}
