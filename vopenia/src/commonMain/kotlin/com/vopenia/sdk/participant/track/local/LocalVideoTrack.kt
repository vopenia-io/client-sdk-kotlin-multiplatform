package com.vopenia.sdk.participant.track.local

import com.vopenia.sdk.participant.track.IVideoTrack
import com.vopenia.sdk.participant.track.VideoSink

expect class LocalVideoTrack : LocalTrack, IVideoTrack {
    override fun addRenderer(videoSink: VideoSink)

    override fun removeRenderer(videoSink: VideoSink)
}
