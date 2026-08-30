package io.vopenia.livekit.effects

import io.vopenia.livekit.participant.effects.VideoEffect
import livekit.org.webrtc.VideoFrame
import livekit.org.webrtc.VideoSink

/**
 * Public, preview-facing facade over the internal [VideoEffectProcessor] so the
 * camera-preview composable (in :vopenia-compose) can run the same blur /
 * background pipeline used for the published in-call track — without the
 * processor itself leaking into the SDK's public surface.
 *
 * Wire it like a WebRTC processor: feed captured frames to [onFrameCaptured] and
 * point [setSink] at the renderer. While the effect is `null` (or the segmenter
 * isn't ready yet) frames pass through unchanged. Call [close] when done to
 * release the worker thread and MediaPipe resources.
 *
 * Construct it OFF the main thread — the constructor loads the MediaPipe model
 * synchronously.
 */
class PreviewVideoEffect {
    private val processor = VideoEffectProcessor().also { it.onCapturerStarted(true) }

    fun setSink(sink: VideoSink?) = processor.setSink(sink)

    fun setEffect(effect: VideoEffect?) {
        processor.currentEffect = effect
    }

    fun onFrameCaptured(frame: VideoFrame) = processor.onFrameCaptured(frame)

    fun close() = processor.close()
}
