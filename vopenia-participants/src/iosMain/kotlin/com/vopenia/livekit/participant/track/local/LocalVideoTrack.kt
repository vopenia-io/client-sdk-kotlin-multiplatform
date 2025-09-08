package com.vopenia.livekit.participant.track.local

import LiveKitClient.LocalVideoTrack
import LiveKitClientKotlin.VideoTrackAddKotlin
import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.VideoSink
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual class LocalVideoTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : LocalTrack(scope, track), IVideoTrack {
    private val delegate = VideoTrackAddKotlin()

    actual override fun addRenderer(videoSink: VideoSink) {
        track.track()?.let {
            delegate.setTrackWithVideoView(videoSink.videoView, it)
        }
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        track.track()?.let {
            delegate.removeWithVideoView(videoSink.videoView)
        }
    }
}
