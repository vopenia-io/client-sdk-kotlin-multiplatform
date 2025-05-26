package com.vopenia.livekit.participant.track.local

import com.vopenia.livekit.participant.track.Kind
import com.vopenia.livekit.participant.track.Source
import com.vopenia.livekit.participant.track.Track

expect sealed class LocalTrack : Track {
    internal val track: LocalTrackPublication

    override val name: String

    override val kind: Kind

    override val sid: String

    override val source: Source

    internal fun updateInternalTrack(track: LocalTrackPublication)
}
