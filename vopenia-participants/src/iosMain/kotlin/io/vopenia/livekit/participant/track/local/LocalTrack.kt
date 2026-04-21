package io.vopenia.livekit.participant.track.local

import io.vopenia.livekit.participant.track.Source
import io.vopenia.livekit.participant.track.Track
import io.vopenia.livekit.participant.track.kindFrom
import io.vopenia.livekit.participant.track.sourceFrom
import io.vopenia.livekit.participant.track.trackStateFromPublication
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual sealed class LocalTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : Track(scope, trackStateFromPublication(track)) {
    private var internalTrack = track

    actual val track: LocalTrackPublication
        get() = internalTrack

    actual override val name = track.name()

    actual override val kind = kindFrom(track.kind())
    actual override val sid = track.sid().stringValue()

    actual override val source: Source = sourceFrom(track.source())

    internal actual fun updateInternalTrack(track: LocalTrackPublication) {
        internalTrack = track
    }
}
