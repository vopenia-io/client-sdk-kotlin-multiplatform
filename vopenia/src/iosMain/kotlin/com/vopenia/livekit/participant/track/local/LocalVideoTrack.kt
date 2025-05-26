package com.vopenia.livekit.participant.track.local

import LiveKitClient.LocalVideoTrack
import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.VideoSink
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual class LocalVideoTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : LocalTrack(scope, track), IVideoTrack {
    actual override fun addRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is LocalVideoTrack) {
                videoSink.videoView.setTrack(it)
            }
        }
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is LocalVideoTrack) {
                videoSink.videoView.setTrack(null)
            }
        }
    }
}
