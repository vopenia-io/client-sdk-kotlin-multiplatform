package com.vopenia.sdk.participant.track

interface IVideoTrack: ITrack {
    fun addRenderer(videoSink: VideoSink)

    fun removeRenderer(videoSink: VideoSink)
}