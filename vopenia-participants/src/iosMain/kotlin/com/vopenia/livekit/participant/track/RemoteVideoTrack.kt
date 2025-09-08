package com.vopenia.livekit.participant.track

import LiveKitClientKotlin.VideoTrackAddKotlin
import com.vopenia.sdk.utils.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import LiveKitClient.RemoteTrackPublication as RTP

@OptIn(ExperimentalForeignApi::class)
actual class RemoteVideoTrack(
    scope: CoroutineScope,
    track: RTP
) : RemoteTrack(scope, track), IVideoTrack {
    private val delegate = VideoTrackAddKotlin()

    actual override fun addRenderer(videoSink: VideoSink) {
        Log.d("RemoteVideoTrack", "addRenderer ${track.track()}")
        track.track()?.let {
            delegate.setTrackWithVideoView(videoSink.videoView, it)
        }
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        Log.d("RemoteVideoTrack", "removeRenderer")
        delegate.removeWithVideoView(videoSink.videoView)
    }
}
