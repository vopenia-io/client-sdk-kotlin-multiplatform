package com.vopenia.livekit.participant.track

import com.vopenia.livekit.participant.track.local.LocalTrackPublication
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun trackStateFromPublication(track: LocalTrackPublication) = TrackState(
    subscribed = track.isSubscribed(),
    published = false,
    active = false,
    muted = track.isMuted()
)

@OptIn(ExperimentalForeignApi::class)
actual fun trackStateFromPublication(track: RemoteTrackPublication) = TrackState(
    subscribed = track.isSubscribed(),
    published = false,
    active = track.isEnabled(),
    muted = track.isMuted()
)
