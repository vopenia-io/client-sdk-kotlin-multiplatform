package io.vopenia.livekit.participant.track

import LiveKitClientKotlin.TrackPublicationKotlin
import io.vopenia.livekit.participant.track.local.LocalTrackPublication
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun trackStateFromPublication(track: LocalTrackPublication) = TrackState(
    subscribed = track.isSubscribed(),
    published = false,
    active = false,
    muted = track.isMuted(),
    width = TrackPublicationKotlin.widthOf(track).toInt(),
    height = TrackPublicationKotlin.heightOf(track).toInt(),
)

@OptIn(ExperimentalForeignApi::class)
actual fun trackStateFromPublication(track: RemoteTrackPublication) = TrackState(
    subscribed = track.isSubscribed(),
    published = false,
    active = track.isEnabled(),
    muted = track.isMuted(),
    width = TrackPublicationKotlin.widthOf(track).toInt(),
    height = TrackPublicationKotlin.heightOf(track).toInt(),
)
