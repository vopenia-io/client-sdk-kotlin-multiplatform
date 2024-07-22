package com.vopenia.sdk.participant.track

import io.livekit.android.room.track.Track

fun Track.Source.toSource(): Source {
    return when (this) {
        Track.Source.CAMERA -> Source.CAMERA
        Track.Source.MICROPHONE -> Source.MICROPHONE
        Track.Source.SCREEN_SHARE -> Source.SCREEN_SHARE
        Track.Source.UNKNOWN -> Source.UNKNOWN
    }
}
