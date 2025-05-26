package com.vopenia.livekit

import LiveKitClient.VideoView
import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.VideoSink
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

    override fun attach(track: IVideoTrack) {
        track.addRenderer(this)
    }

    override fun detach(track: IVideoTrack) {
        track.removeRenderer(this)
    }
}
