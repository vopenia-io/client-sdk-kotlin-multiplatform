package com.vopenia.livekit.participant.track.local

import LiveKitClient.LocalVideoTrack
import LiveKitClient.createCameraTrack
import LiveKitClientKotlin.VideoTrackAddKotlin
import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.TrackState
import com.vopenia.livekit.participant.track.VideoSink
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalForeignApi::class)
class LocalVideoTrackPreview() : IVideoTrack {
    private val track = LocalVideoTrack.createCameraTrack()

    private val delegate = VideoTrackAddKotlin()

    fun start() {
        track.startWithCompletionHandler { error ->
            println("error : ${error?.localizedDescription}")
        }
    }

    fun stop() {
        track.stopWithCompletionHandler { error ->
            println("error : ${error?.localizedDescription}")
        }
    }

    override fun addRenderer(videoSink: VideoSink) {
        delegate.setTrackWithVideoView(videoSink.videoView, track)
    }

    override fun removeRenderer(videoSink: VideoSink) {
        delegate.removeWithVideoView(videoSink.videoView)
    }

    override val state = MutableStateFlow(
        TrackState(
            subscribed = true,
            published = false,
            active = true,
            muted = false
        )
    )
}
