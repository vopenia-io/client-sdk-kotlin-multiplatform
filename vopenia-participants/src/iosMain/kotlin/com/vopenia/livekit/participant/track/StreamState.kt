package com.vopenia.livekit.participant.track

import platform.darwin.NSInteger

enum class StreamState(private val native: NSInteger) {
    Pause(0),
    Active(1);

    companion object {
        fun fromNSInteger(native: NSInteger) = entries.firstOrNull { it.native == native }
    }
}