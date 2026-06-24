package io.vopenia.livekit.effects

import android.graphics.Bitmap
import android.graphics.Matrix
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.sdk.utils.Log
import livekit.org.webrtc.VideoFrame
import livekit.org.webrtc.VideoProcessor
import livekit.org.webrtc.VideoSink
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * WebRTC [VideoProcessor] intercepting each captured camera frame, downscaling
 * it to [PROCESS_MAX_SIDE], running MediaPipe selfie segmentation, and
 * compositing a blurred / image background with the segmented foreground before
 * forwarding to the downstream [VideoSink]. Frames are also rotated upright so
 * the replacement background isn't turned by the renderer and the output is
 * emitted with rotation=0.
 *
 * Threading: WebRTC delivers frames on its capture thread; we process them on a
 * dedicated single worker thread and never block the caller. Blocking the capture
 * thread (the old synchronous path) froze the whole preview for the ~1-2s of
 * MediaPipe graph init on attach and throttled capture to the processing rate.
 * Memory/CPU: the V1 path is intentionally CPU-bound (Bitmap+Canvas + per-pixel
 * YUV<->Bitmap loops). Cost scales quadratically with resolution, hence the
 * [PROCESS_MAX_SIDE] cap; GPU shaders + buffer pooling are the V2 optimisation.
 */
internal class VideoEffectProcessor : VideoProcessor {

    @Volatile
    var currentEffect: VideoEffect? = null

    private val segmenter = MediaPipeSegmenter()
    private val compositor = EffectCompositor()

    @Volatile
    private var sink: VideoSink? = null

    // Heavy work runs here, off the capture thread. We keep only the most recent
    // unprocessed frame ([pending]); older ones are dropped so the worker can't
    // fall behind the camera. MediaPipe is always called from this one thread, so
    // its single-thread affinity is respected.
    private val worker: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "VideoEffectWorker")
    }
    private val pending = AtomicReference<VideoFrame?>(null)

    // Frame-rate cap pacing (capture thread only).
    @Volatile
    private var lastAcceptedNanos = 0L

    // Processing budget in PIXELS (area), adapted to the device at runtime (see
    // logTiming). Capping by area rather than longest side keeps the per-frame
    // cost independent of the capture aspect ratio (a 4:3 frame has more pixels
    // than a 16:9 frame at the same longest side).
    @Volatile
    private var processMaxArea = INITIAL_MAX_AREA

    // Temporal mask reuse: MediaPipe segmentation is the dominant per-frame cost
    // (~60-70 ms on a Galaxy S9). We run it only every [SEGMENT_EVERY]th frame and
    // reuse the last mask in between — the foreground pixels still update every
    // frame; only the cutout edge lags slightly on fast motion. Big CPU/battery
    // saving. cachedMask is read/written on the worker thread; @Volatile lets
    // onCapturerStopped() reset it from the capture thread.
    @Volatile
    private var cachedMask: SegmentationMask? = null
    private var processIndex = 0L

    override fun onCapturerStarted(success: Boolean) {
        Log.d(TAG, "onCapturerStarted success=$success")
    }

    override fun onCapturerStopped() {
        Log.d(TAG, "onCapturerStopped")
        // Drop the reused mask so a restart re-segments from a fresh frame.
        cachedMask = null
    }

    override fun setSink(sink: VideoSink?) {
        this.sink = sink
    }

    override fun onFrameCaptured(frame: VideoFrame) = enqueue(frame)

    override fun onFrameCaptured(
        frame: VideoFrame,
        parameters: VideoProcessor.FrameAdaptationParameters
    ) {
        // V1: ignore adaptation parameters and emit at the processing resolution.
        // WebRTC may still downscale downstream — the publisher path adapts.
        enqueue(frame)
    }

    private fun enqueue(frame: VideoFrame) {
        if (currentEffect == null || !segmenter.isReady) {
            sink?.onFrame(frame) // nothing to apply — forward unchanged
            return
        }
        // Frame-rate cap (battery): processing every captured frame (often 30 fps)
        // burns CPU for little gain on a background effect. Drop frames arriving
        // sooner than the target interval — the encoder/renderers simply hold the
        // last one. A dropped frame costs nothing: the capture thread returns at once.
        val now = System.nanoTime()
        if (now - lastAcceptedNanos < MIN_FRAME_INTERVAL_NANOS) return
        lastAcceptedNanos = now

        // Retain across the thread hop; replace (and release) any stale pending frame.
        frame.retain()
        pending.getAndSet(frame)?.release()
        runCatching { worker.execute(::drain) }
            .onFailure { pending.getAndSet(null)?.release() } // worker shut down
    }

    private fun drain() {
        val frame = pending.getAndSet(null) ?: return
        try {
            val out = processFrame(frame)
            if (out != null) {
                sink?.onFrame(out)
                out.release()
            }
        } finally {
            frame.release()
        }
    }

    private fun processFrame(frame: VideoFrame): VideoFrame? {
        val effect = currentEffect ?: return null
        if (!segmenter.isReady) return null

        val started = System.nanoTime()
        val rotation = frame.rotation

        // Downscale in the WebRTC buffer (cheap, often native) BEFORE the two
        // pure-Kotlin per-pixel YUV<->Bitmap loops, which dominate cost — they
        // scale quadratically with resolution. At full 720p the whole pipeline
        // ran ~3 fps on a Galaxy S9, so the local self-view looked frozen even
        // though the far-end received the (slow) frames. Processing at a capped
        // resolution restores a smooth preview; the encoder/renderers upscale.
        val srcW = frame.buffer.width
        val srcH = frame.buffer.height
        val (procW, procH) = scaledDimensions(srcW, srcH)
        val scaled = frame.buffer.cropAndScale(0, 0, srcW, srcH, procW, procH)
        val i420 = runCatching { scaled.toI420() }.getOrNull().also { scaled.release() }
            ?: return null
        try {
            val raw = runCatching { YuvBitmapConverter.i420ToBitmap(i420) }
                .onFailure { Log.d(TAG, "i420ToBitmap failed: $it") }
                .getOrNull() ?: return null

            // Bring the frame upright *before* segmenting/compositing. The camera
            // delivers buffers in sensor orientation with a separate `rotation`
            // (typically 90/270 in portrait). If we composited in sensor space and
            // re-emitted `rotation`, the downstream renderer would rotate the whole
            // frame — leaving the replacement background visibly sideways — and
            // MediaPipe would segment a lying-down person (poor masks). So we rotate
            // the bitmap to upright, work there, and emit the result with rotation=0.
            val bitmap = uprightCopy(raw, rotation)
            if (bitmap !== raw) raw.recycle()

            // Segment every SEGMENT_EVERY-th frame; reuse the last mask otherwise.
            // The cached mask is resolution-agnostic — compose rescales it to the
            // current frame size — so it stays valid even when the adaptive
            // resolution changes.
            val mask = if (processIndex % SEGMENT_EVERY == 0L || cachedMask == null) {
                segmenter.segment(bitmap)?.also { cachedMask = it }
            } else {
                cachedMask
            }
            processIndex++
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

            logTiming(frame, rotation, newBuffer.width, newBuffer.height, started)
            return VideoFrame(newBuffer, 0, frame.timestampNs)
        } finally {
            i420.release()
        }
    }

    /** Rotates [src] clockwise by [rotation] degrees so the content is upright. */
    private fun uprightCopy(src: Bitmap, rotation: Int): Bitmap {
        if (rotation % 360 == 0) return src
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    /**
     * Scales (w, h) down so its area fits the current adaptive [processMaxArea],
     * preserving aspect ratio and keeping both dimensions even (I420 chroma is
     * 2x2 sub-sampled). Returns the source size unchanged when it already fits.
     */
    private fun scaledDimensions(w: Int, h: Int): Pair<Int, Int> {
        val area = w.toLong() * h.toLong()
        if (area <= processMaxArea) return w to h
        val scale = sqrt(processMaxArea.toDouble() / area.toDouble())
        val sw = max(2, (w * scale).toInt() and 1.inv())
        val sh = max(2, (h * scale).toInt() and 1.inv())
        return sw to sh
    }

    private var frameCount = 0L
    private var accumNanos = 0L

    /**
     * Throttled (every 30 frames): logs geometry + avg processing time and adapts
     * [processMaxSide] to the device — climb on fast CPUs for sharper output, back
     * off on slow ones to stay smooth and save battery. Hysteresis (a dead zone
     * between [FAST_MS] and [SLOW_MS]) and the ~1.5 s cadence avoid resolution
     * thrashing; on a mid device it settles and stays put.
     */
    private fun logTiming(frame: VideoFrame, inRotation: Int, outW: Int, outH: Int, startedNanos: Long) {
        accumNanos += System.nanoTime() - startedNanos
        frameCount++
        if (frameCount % 30 == 0L) {
            val avgMs = accumNanos / 30 / 1_000_000.0
            processMaxArea = when {
                avgMs > SLOW_MS -> max(MIN_AREA, (processMaxArea * SHRINK).toInt())
                avgMs < FAST_MS -> min(MAX_AREA, (processMaxArea * GROW).toInt())
                else -> processMaxArea
            }
            Log.d(
                TAG,
                "effect frame#$frameCount in=${frame.buffer.width}x${frame.buffer.height} rot=$inRotation" +
                    " -> out=${outW}x${outH} rot=0 avg=${"%.1f".format(avgMs)}ms" +
                    " (~${"%.1f".format(1000.0 / avgMs)}fps) nextMaxArea=$processMaxArea"
            )
            accumNanos = 0L
        }
    }

    fun close() {
        // Tear down on the worker so any in-flight frame finishes and the native
        // MediaPipe / RenderScript handles are released off the capture/UI thread.
        runCatching {
            worker.execute {
                pending.getAndSet(null)?.release()
                cachedMask = null
                runCatching { segmenter.close() }
                runCatching { compositor.close() }
            }
        }
        worker.shutdown()
    }

    companion object {
        private const val TAG = "VideoEffectProcessor"

        // Frame-rate cap. The effect output never exceeds this; the camera may
        // capture faster (e.g. 30 fps) but extra frames are dropped to save CPU /
        // battery. 20 fps is smooth for a background effect; slow devices fall
        // below it naturally (worker-bound) so the cap only trims fast devices.
        private const val TARGET_FPS = 20
        private const val MIN_FRAME_INTERVAL_NANOS = 1_000_000_000L / TARGET_FPS

        // Run MediaPipe segmentation once every Nth processed frame and reuse the
        // mask in between. 2 ≈ halves the dominant cost; the cutout edge lags one
        // frame on fast motion (imperceptible for a call background).
        private const val SEGMENT_EVERY = 2

        // Adaptive processing budget in PIXELS (area). Frames are scaled to fit
        // this before the CPU pipeline and published at that size; the
        // encoder/renderers upscale. Initial ≈ 480x270 (16:9) / 416x312 (4:3) —
        // the resolution validated as smooth on a Galaxy S9. Bounds: ~320x180 up
        // to ~720x405-class. GROW/SHRINK adjust multiplicatively each ~1.5 s.
        private const val INITIAL_MAX_AREA = 130_000
        private const val MIN_AREA = 60_000
        private const val MAX_AREA = 290_000
        private const val GROW = 1.25
        private const val SHRINK = 0.75

        // Per-frame time thresholds (ms) for adapting the area. Below FAST_MS we
        // have headroom → grow; above SLOW_MS we're really struggling (<10 fps) →
        // shrink. The wide dead zone keeps mid devices stable and never shrinks
        // below the validated experience just because MediaPipe's CPU inference
        // (~60-70 ms on a Galaxy S9) is the floor — shrinking can't beat it.
        private const val FAST_MS = 40.0
        private const val SLOW_MS = 100.0
    }
}
