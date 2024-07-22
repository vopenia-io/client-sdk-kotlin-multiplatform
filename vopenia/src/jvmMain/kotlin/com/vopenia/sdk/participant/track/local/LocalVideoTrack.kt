package com.vopenia.sdk.participant.track.local

import com.vopenia.sdk.participant.track.IVideoTrack
import com.vopenia.sdk.participant.track.VideoSink

actual class LocalVideoTrack : LocalTrack(), IVideoTrack {
    actual override fun addRenderer(videoSink: VideoSink) {
        TODO("Not yet implemented")
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        TODO("Not yet implemented")
    }
}
