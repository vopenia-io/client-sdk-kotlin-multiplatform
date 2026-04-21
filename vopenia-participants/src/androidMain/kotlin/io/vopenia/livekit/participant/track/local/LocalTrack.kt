package io.vopenia.livekit.participant.track.local

import io.vopenia.livekit.participant.track.kindFrom
import io.vopenia.livekit.participant.track.toSource
import io.vopenia.livekit.participant.track.trackStateFromPublication
import kotlinx.coroutines.CoroutineScope

actual sealed class LocalTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : io.vopenia.livekit.participant.track.Track(scope, trackStateFromPublication(track)) {
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
