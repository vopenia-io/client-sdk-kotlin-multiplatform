package com.vopenia.livekit.participant.track

import com.vopenia.livekit.participant.track.local.LocalTrackPublication
import io.livekit.android.room.track.Track

actual fun trackStateFromPublication(track: LocalTrackPublication) = TrackState(
    subscribed = track.subscribed,
    published = true, // ?
    active = track.track?.streamState == Track.StreamState.ACTIVE,
    muted = track.muted
)

actual fun trackStateFromPublication(track: RemoteTrackPublication) = TrackState(
    subscribed = track.subscribed,
    published = track.track?.streamState == Track.StreamState.ACTIVE, // ?
    active = track.track?.streamState == Track.StreamState.ACTIVE,
    muted = track.muted
)
