package io.vopenia.livekit.participant.track

import android.view.View
import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.room.track.video.ViewVisibility
import kotlinx.coroutines.CoroutineScope
import io.livekit.android.room.track.RemoteVideoTrack as LiveKitRemoteVideoTrack

actual class RemoteVideoTrack(
    scope: CoroutineScope,
    track: RemoteTrackPublication
) : RemoteTrack(scope, track), IVideoTrack {
    /**
     * Attaching a renderer is also what tells an adaptive room that the track is on
     * screen: LiveKit keeps every track paused until one of its renderers reports
     * itself visible, and it learns that from the view's layout callbacks. Those have
     * already fired by the time Compose attaches the renderer to a view it just laid
     * out, so the track would stay paused for the whole call — the far end's video
     * never arrives and the tile falls back to the avatar. Owning the visibility here
     * lets us measure the view once, right now, instead of waiting for a layout pass
     * that may never come; LiveKit keeps updating it from then on.
     */
    actual override fun addRenderer(videoSink: VideoSink) {
        val media = track.track
        if (media !is VideoTrack) return

        if (media is LiveKitRemoteVideoTrack && videoSink is View) {
            val visibility = ViewVisibility(videoSink)
            media.addRenderer(videoSink, visibility)
            visibility.recalculate()
        } else {
            media.addRenderer(videoSink)
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
