package io.vopenia.livekit.participant.track.local

import io.vopenia.livekit.participant.track.IVideoTrack
import io.vopenia.livekit.participant.track.VideoSink
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope

actual class LocalVideoTrack(
    scope: CoroutineScope,
    track: LocalTrackPublication
) : LocalTrack(scope, track), IVideoTrack {
    actual override fun addRenderer(videoSink: VideoSink) {
        track.track?.let {
            if (it is VideoTrack) {
                // Render the local self-view from the post-VideoSource rtcTrack rather
                // than LiveKit's addRenderer(). addRenderer() registers the sink on the
                // CaptureDispatchObserver, which feeds local renderers the RAW capturer
                // frame — upstream of the VideoSource and therefore of our VideoProcessor.
                // So any effect (background replacement, blur) stays invisible locally
                // while remote peers see it. rtcTrack sits downstream of the source, so
                // the preview shows exactly what is published.
                it.rtcTrack.addSink(videoSink)
            }
        }
    }

    actual override fun removeRenderer(videoSink: VideoSink) {
        track.track?.let {
            if (it is VideoTrack) {
                it.rtcTrack.removeSink(videoSink)
            }
        }
    }
}
