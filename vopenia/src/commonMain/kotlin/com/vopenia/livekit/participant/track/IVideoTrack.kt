package com.vopenia.livekit.participant.track

interface IVideoTrack : ITrack {
    fun addRenderer(videoSink: VideoSink)

    fun removeRenderer(videoSink: VideoSink)
}
