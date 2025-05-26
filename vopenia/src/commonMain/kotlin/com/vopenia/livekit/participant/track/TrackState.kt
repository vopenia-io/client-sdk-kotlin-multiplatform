package com.vopenia.livekit.participant.track

import com.vopenia.livekit.participant.track.local.LocalTrackPublication

data class TrackState(
    val subscribed: Boolean = false,
    val published: Boolean = false,
    val active: Boolean = false,
    val muted: Boolean = false
)

expect fun trackStateFromPublication(track: LocalTrackPublication): TrackState

expect fun trackStateFromPublication(track: RemoteTrackPublication): TrackState
