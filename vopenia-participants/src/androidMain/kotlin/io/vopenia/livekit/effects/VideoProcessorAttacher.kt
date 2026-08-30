package io.vopenia.livekit.effects

import io.livekit.android.room.track.LocalVideoTrack
import io.vopenia.sdk.utils.Log
import livekit.org.webrtc.VideoProcessor
import livekit.org.webrtc.VideoSource

/**
 * Attach (or detach) a [VideoProcessor] on a LiveKit [LocalVideoTrack].
 *
 * LiveKit Android 2.16 doesn't expose a public setter for `VideoProcessor`
 * after track creation — the public APIs (`setCameraEnabled`, etc.) thread it
 * only at construction via `internal` factories. We bypass via reflection on
 * the private `source: VideoSource` field, then call WebRTC's
 * `VideoSource.setVideoProcessor()` (which IS public).
 *
 * This is fragile against LiveKit SDK refactors — if the field name changes,
 * the call is a logged no-op and the frame pipeline keeps publishing the raw
 * camera. Verify after each `livekit-android` bump.
 */
internal object VideoProcessorAttacher {

    fun attach(track: LocalVideoTrack, processor: VideoProcessor?): Boolean = runCatching {
        val field = track.javaClass.getDeclaredField("source").apply { isAccessible = true }
        val source = field.get(track) as? VideoSource
            ?: throw IllegalStateException("LocalVideoTrack.source is not a VideoSource")
        source.setVideoProcessor(processor)
        true
    }.onFailure {
        Log.d(TAG, "failed to attach VideoProcessor: $it")
    }.getOrDefault(false)

    private const val TAG = "VideoProcessorAttacher"
}
