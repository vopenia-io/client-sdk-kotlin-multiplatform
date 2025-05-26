package com.vopenia.livekit.participant.track.local

import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.VideoSink
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope

actual class LocalVideoTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : LocalTrack(scope, track), IVideoTrack {
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
