package com.vopenia.livekit.participant.track

import LiveKitClient.RemoteTrackPublication
import LiveKitClient.RemoteVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual class RemoteVideoTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track), IVideoTrack {
    actual override fun addRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is RemoteVideoTrack) {
                videoSink.videoView.setTrack(it)
            }
        }
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is RemoteVideoTrack) {
                videoSink.videoView.setTrack(null)
            }
        }
    }
}
