package io.vopenia.livekit.participant.track.local

import LiveKitClient.LocalVideoTrack
import LiveKitClientKotlin.VideoTrackAddKotlin
import io.vopenia.livekit.participant.track.IVideoTrack
import io.vopenia.livekit.participant.track.VideoSink
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

    /**
     * The underlying native LiveKit `VideoTrack` for an AVKit sink (Picture-in-
     * Picture), as an opaque [platform.darwin.NSObject] so no LiveKit type leaks into
     * the consuming framework header. Mirrors `RemoteVideoTrack.nativeVideoTrack`;
     * used for the local "you" tile in the iOS call PiP. Null until the camera track
     * is published.
     */
    fun nativeVideoTrack(): platform.darwin.NSObject? = track.track()
}
