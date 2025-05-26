package com.vopenia.livekit.participant.track.local

import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.VideoSink

actual class LocalVideoTrack : LocalTrack(), IVideoTrack {
    actual override fun addRenderer(videoSink: VideoSink) {
        TODO("Not yet implemented")
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        TODO("Not yet implemented")
    }
}
