package com.vopenia.sdk

import LiveKitClient.RemoteVideoTrack
import LiveKitClient.VideoView
import com.vopenia.sdk.participant.track.RemoteTrack
import com.vopenia.sdk.participant.track.VideoSink
import kotlinx.cinterop.ExperimentalForeignApi

object VideoViewFactory {
    @OptIn(ExperimentalForeignApi::class)
    fun createVideoView() = VideoViewWrapper(VideoView())
}

@OptIn(ExperimentalForeignApi::class)
class VideoViewWrapper(
    override val videoView: VideoView
) : VideoSink {
    // var track: RemoteTrack? = null

    override fun attach(track: RemoteTrack) {
        track.addRenderer(this)
    }

    override fun detach(track: RemoteTrack) {
        track.removeRenderer(this)
    }
}