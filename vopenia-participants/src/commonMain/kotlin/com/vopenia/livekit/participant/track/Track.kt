package com.vopenia.livekit.participant.track

import kotlinx.coroutines.CoroutineScope

abstract class Track(
    scope: CoroutineScope,
    defaultState: TrackState
) : SubTrack(scope, defaultState) {
    abstract val name: String

    abstract val kind: Kind

    abstract val sid: String

    abstract val source: Source
}
