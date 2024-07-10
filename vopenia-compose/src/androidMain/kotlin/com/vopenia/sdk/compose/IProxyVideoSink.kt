package com.vopenia.sdk.compose

import livekit.org.webrtc.VideoSink

interface IProxyVideoSink {
    fun register(videoSink: VideoSink)

    fun unregister(videoSink: VideoSink)
}
