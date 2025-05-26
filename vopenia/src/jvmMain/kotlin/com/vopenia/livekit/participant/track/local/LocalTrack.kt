package com.vopenia.livekit.participant.track.local

import com.vopenia.livekit.participant.track.Kind
import com.vopenia.livekit.participant.track.Source
import com.vopenia.livekit.participant.track.Track
import com.vopenia.livekit.participant.track.TrackState
import com.vopenia.sdk.utils.Dispatchers
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
