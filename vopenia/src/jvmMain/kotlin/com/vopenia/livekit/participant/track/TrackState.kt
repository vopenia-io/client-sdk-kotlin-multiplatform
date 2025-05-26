package com.vopenia.livekit.participant.track

import com.vopenia.livekit.participant.track.local.LocalTrackPublication

actual fun trackStateFromPublication(track: LocalTrackPublication) = TrackState(
    subscribed = false,
    published = false,
    active = false,
    muted = false
)

actual fun trackStateFromPublication(track: RemoteTrackPublication) = TrackState(
    subscribed = false,
    published = false,
    active = false,
    muted = false
)
