package com.vopenia.sdk.participant.track

expect class RemoteVideoTrack : RemoteTrack, IVideoTrack {
    override fun addRenderer(videoSink: VideoSink)

    override fun removeRenderer(videoSink: VideoSink)
}
