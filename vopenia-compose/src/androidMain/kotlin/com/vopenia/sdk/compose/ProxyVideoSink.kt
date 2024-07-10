package com.vopenia.sdk.compose

import livekit.org.webrtc.VideoFrame
import livekit.org.webrtc.VideoSink

class ProxyVideoSink : IProxyVideoSink, VideoSink {
    private var videoSink: VideoSink? = null
    override fun register(videoSink: VideoSink) {
        this.videoSink = videoSink
    }

    override fun unregister(videoSink: VideoSink) {
        if (this.videoSink == videoSink) this.videoSink = null
    }

    override fun onFrame(frame: VideoFrame?) {
        videoSink?.onFrame(frame)
    }
}
