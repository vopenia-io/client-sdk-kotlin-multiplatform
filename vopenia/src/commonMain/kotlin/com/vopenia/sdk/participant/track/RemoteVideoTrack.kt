package com.vopenia.sdk.participant.track

expect class RemoteVideoTrack : RemoteTrack {
    fun addRenderer(videoSink: VideoSink)

    fun removeRenderer(videoSink: VideoSink)
}
