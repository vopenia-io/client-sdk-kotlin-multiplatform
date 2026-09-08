package io.vopenia.livekit.participant.track

import io.vopenia.livekit.participant.track.local.LocalTrackPublication

data class TrackState(
    val subscribed: Boolean = false,
    val published: Boolean = false,
    val active: Boolean = false,
    val muted: Boolean = false,
    /**
     * Published video dimensions (from the publication's TrackInfo, i.e. what
     * the publisher declared - a screen share's capture size); 0 when unknown
     * or for audio. Lets a client lay out around a letterboxed track.
     */
    val width: Int = 0,
    val height: Int = 0,
)

expect fun trackStateFromPublication(track: LocalTrackPublication): TrackState

expect fun trackStateFromPublication(track: RemoteTrackPublication): TrackState
