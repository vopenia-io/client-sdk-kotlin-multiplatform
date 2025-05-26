package com.vopenia.livekit.participant.track

import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope

actual class RemoteVideoTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track), IVideoTrack {
    actual override fun addRenderer(videoSink: VideoSink) {
        println("VideoView -> addRenderer called ${track.track}")
        track.track?.let {
            if (it is VideoTrack) {
                it.addRenderer(videoSink)
            }
        }
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        track.track?.let {
            if (it is VideoTrack) {
                it.removeRenderer(videoSink)
            }
        }
    }
}
