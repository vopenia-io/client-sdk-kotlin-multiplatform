package io.vopenia.livekit.participant.track

import LiveKitClientKotlin.VideoTrackAddKotlin
import io.vopenia.sdk.utils.Log
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

    /**
     * The underlying native LiveKit `VideoTrack` for an AVKit sink (Picture-in-
     * Picture), returned as an opaque [platform.darwin.NSObject] so NO LiveKit type
     * leaks into the consuming framework header / cinterop (that leak is what broke
     * the earlier bridge-based attempt). iosApp casts it back to `VideoTrack`. Same
     * handle [addRenderer] attaches to; null until the track is subscribed.
     */
    fun nativeVideoTrack(): platform.darwin.NSObject? = track.track()
}
