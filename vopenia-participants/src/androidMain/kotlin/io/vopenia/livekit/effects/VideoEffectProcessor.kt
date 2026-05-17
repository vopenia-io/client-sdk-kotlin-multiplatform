package io.vopenia.livekit.effects

import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.sdk.utils.Log
import livekit.org.webrtc.VideoFrame
import livekit.org.webrtc.VideoProcessor
import livekit.org.webrtc.VideoSink

/**
 * WebRTC [VideoProcessor] intercepting each captured camera frame, running
 * MediaPipe selfie segmentation on a downscaled copy, and compositing a
 * blurred / image background with the segmented foreground before forwarding
 * to the downstream [VideoSink].
 *
 * Threading: WebRTC invokes us on its own capture thread — we stay synchronous.
 * Memory: the V1 path is intentionally CPU-bound (Bitmap+Canvas). Allocations
 * per frame are real and noticeable above 720p — buffer pooling is the natural
 * V2 optimisation.
 */
internal class VideoEffectProcessor : VideoProcessor {

    @Volatile
    var currentEffect: VideoEffect? = null

    private val segmenter = MediaPipeSegmenter()
    private val compositor = EffectCompositor()

    @Volatile
    private var sink: VideoSink? = null

    override fun onCapturerStarted(success: Boolean) {
        Log.d(TAG, "onCapturerStarted success=$success")
    }

    override fun onCapturerStopped() {
        Log.d(TAG, "onCapturerStopped")
    }

    override fun setSink(sink: VideoSink?) {
        this.sink = sink
    }

    override fun onFrameCaptured(frame: VideoFrame) {
        deliver(processFrame(frame) ?: frame)
    }

    override fun onFrameCaptured(
        frame: VideoFrame,
        parameters: VideoProcessor.FrameAdaptationParameters
    ) {
        // V1: ignore adaptation parameters and emit at native capture resolution.
        // WebRTC may still downscale downstream — the publisher path adapts.
        deliver(processFrame(frame) ?: frame)
    }

    private fun deliver(frame: VideoFrame) {
        sink?.onFrame(frame)
    }

    private fun processFrame(frame: VideoFrame): VideoFrame? {
        val effect = currentEffect ?: return null
        if (!segmenter.isReady) return null

        val i420 = frame.buffer.toI420() ?: return null
        try {
            val bitmap = runCatching { YuvBitmapConverter.i420ToBitmap(i420) }
                .onFailure { Log.d(TAG, "i420ToBitmap failed: $it") }
                .getOrNull() ?: return null

            val mask = segmenter.segment(bitmap)
            if (mask == null) {
                bitmap.recycle()
                return null
            }
            val composed = compositor.compose(bitmap, mask, effect)
            bitmap.recycle()
            if (composed == null) return null

            val newBuffer = runCatching { YuvBitmapConverter.bitmapToI420(composed) }
                .onFailure { Log.d(TAG, "bitmapToI420 failed: $it") }
                .getOrNull()
            composed.recycle()
            if (newBuffer == null) return null

            return VideoFrame(newBuffer, frame.rotation, frame.timestampNs)
        } finally {
            i420.release()
        }
    }

    fun close() {
        runCatching { segmenter.close() }
        runCatching { compositor.close() }
    }

    companion object {
        private const val TAG = "VideoEffectProcessor"
    }
}
