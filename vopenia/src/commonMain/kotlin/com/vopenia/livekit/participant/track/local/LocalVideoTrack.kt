package com.vopenia.livekit.participant.track.local

import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.VideoSink

expect class LocalVideoTrack : LocalTrack,
    IVideoTrack {
    override fun addRenderer(videoSink: VideoSink)

    override fun removeRenderer(videoSink: VideoSink)
}
