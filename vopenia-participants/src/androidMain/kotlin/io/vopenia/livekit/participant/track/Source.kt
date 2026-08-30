package io.vopenia.livekit.participant.track

import io.livekit.android.room.track.Track

fun Track.Source.toSource(): Source {
    return when (this) {
        Track.Source.CAMERA -> Source.CAMERA
        Track.Source.MICROPHONE -> Source.MICROPHONE
        Track.Source.SCREEN_SHARE -> Source.SCREEN_SHARE
        Track.Source.UNKNOWN -> Source.UNKNOWN
        Track.Source.SCREEN_SHARE_AUDIO -> Source.SCREEN_SHARE_AUDIO
    }
}

internal fun Source.toLkSource(): Track.Source = when (this) {
    Source.CAMERA -> Track.Source.CAMERA
    Source.MICROPHONE -> Track.Source.MICROPHONE
    Source.SCREEN_SHARE -> Track.Source.SCREEN_SHARE
    Source.SCREEN_SHARE_AUDIO -> Track.Source.SCREEN_SHARE_AUDIO
    Source.UNKNOWN -> Track.Source.UNKNOWN
}
