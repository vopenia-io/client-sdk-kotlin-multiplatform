package io.vopenia.livekit.participant.effects

/**
 * Visual effect applied to the local participant's camera feed before
 * publishing. Effects are mutually exclusive — applying a new one replaces
 * the previous, and `setVideoEffect(null)` returns to the raw camera output.
 *
 * Pipeline:
 * - Android: MediaPipe Tasks Vision (GPU delegate) for person segmentation,
 *   then OpenGL ES shaders for blur/composite.
 * - iOS: Apple Vision (`VNGeneratePersonSegmentationRequest`) +
 *   CoreImage for blur/composite. All native — no third-party model.
 * - JVM: not supported.
 */
sealed class VideoEffect {
    /** Mildly blurred background. Lower compute, suitable for low-tier devices. */
    object BlurLight : VideoEffect()

    /** Strongly blurred background. */
    object BlurStrong : VideoEffect()

    /** Replace the background with a still image. */
    data class Background(val image: BackgroundImage) : VideoEffect()
}
