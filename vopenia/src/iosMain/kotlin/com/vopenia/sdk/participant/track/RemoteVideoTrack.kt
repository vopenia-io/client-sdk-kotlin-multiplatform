package com.vopenia.sdk.participant.track

import LiveKitClient.RemoteTrackPublication
import LiveKitClient.RemoteVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalForeignApi::class)
actual class RemoteVideoTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track) {
    actual fun addRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is RemoteVideoTrack) {
                videoSink.videoView.setTrack(it)
            }
        }
    }

    actual fun removeRenderer(videoSink: VideoSink) {
        track.track()?.let {
            if (it is RemoteVideoTrack) {
                videoSink.videoView.setTrack(null)
            }
        }
    }
}
