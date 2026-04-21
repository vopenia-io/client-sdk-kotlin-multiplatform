package io.vopenia.livekit.participant.track.local

import io.vopenia.livekit.participant.track.Kind
import io.vopenia.livekit.participant.track.Source
import io.vopenia.livekit.participant.track.Track

expect sealed class LocalTrack : Track {
    internal val track: LocalTrackPublication

    override val name: String

    override val kind: Kind

    override val sid: String

    override val source: Source

    internal fun updateInternalTrack(track: LocalTrackPublication)
}
