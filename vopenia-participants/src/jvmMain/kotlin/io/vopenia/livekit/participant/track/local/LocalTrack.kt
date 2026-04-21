package io.vopenia.livekit.participant.track.local

import io.vopenia.livekit.participant.track.Kind
import io.vopenia.livekit.participant.track.Source
import io.vopenia.livekit.participant.track.Track
import io.vopenia.livekit.participant.track.TrackState
import io.vopenia.sdk.utils.Dispatchers
import kotlinx.coroutines.CoroutineScope

actual sealed class LocalTrack : Track(
    CoroutineScope(
        Dispatchers.Default
    ),
    TrackState()
) {
    actual val track = InternalLocalTrackPublication("NOTHING")

    actual override val name = "NOTHING"

    actual override val kind = Kind.None

    actual override val sid = ""

    internal actual fun updateInternalTrack(track: LocalTrackPublication) {
        // nothing
    }

    actual override val source = Source.UNKNOWN
}
