package io.vopenia.livekit.effects

import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter
import io.vopenia.livekit.Sdk
import io.vopenia.sdk.utils.Log
import java.nio.ByteBuffer

/**
 * Wraps MediaPipe Tasks Vision' ImageSegmenter configured for selfie
 * segmentation (person vs background). Runs on the **CPU delegate only**.
 *
 * The GPU (OpenCL) delegate aborts natively during result read-back on some
 * older GPUs — observed on the Galaxy S9 (Mali-G72): a `SIGABRT` deep inside
 * `PacketGetter.getImageWidthFromImageList` while `ImageSegmenter.convertToTaskResult`
 * reads the GPU output packet. It is a native abort on MediaPipe's internal
 * task-runner thread, so no Kotlin `try/catch` can intercept it — it kills the
 * whole process. The delegate *creates* fine, then crashes only at inference,
 * so a "try GPU, fall back on failure" scheme can't catch it either.
 *
 * The model is tiny (~244 KB float16 at `androidMain/assets/selfie_segmenter.tflite`)
 * and MediaPipe resizes the input to the model's fixed resolution, so CPU
 * inference is real-time and stable across the device range.
 */
internal class MediaPipeSegmenter {

    private val segmenter: ImageSegmenter? = createSegmenter(Delegate.CPU)

    private fun createSegmenter(delegate: Delegate): ImageSegmenter? = runCatching {
        val baseOptions = BaseOptions.builder()
            .setDelegate(delegate)
            .setModelAssetPath(MODEL_ASSET)
            .build()
        val options = ImageSegmenter.ImageSegmenterOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setOutputCategoryMask(false)
            .setOutputConfidenceMasks(true)
            .build()
        ImageSegmenter.createFromOptions(Sdk.applicationContext, options)
    }.onFailure {
        Log.d(TAG, "Failed to create ImageSegmenter on $delegate: $it")
    }.getOrNull()

    val isReady: Boolean get() = segmenter != null

    /**
     * Run segmentation on [bitmap]. Returns a [SegmentationMask] whose
     * `floatBuffer` holds `width * height` float32 confidences in `[0, 1]`
     * (1 = foreground / person). Returns null on error.
     */
    fun segment(bitmap: Bitmap): SegmentationMask? {
        val seg = segmenter ?: return null
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = runCatching { seg.segment(mpImage) }
            .onFailure { Log.d(TAG, "segment failed: $it") }
            .getOrNull() ?: return null
        val mask: MPImage = result.confidenceMasks().orElse(null)?.firstOrNull() ?: return null
        val width = mask.width
        val height = mask.height
        // VEC32F1 = single channel float32 (the confidence mask format).
        val source = runCatching {
            ByteBufferExtractor.extract(mask, MPImage.IMAGE_FORMAT_VEC32F1)
        }.onFailure { Log.d(TAG, "ByteBufferExtractor failed: $it") }
            .getOrNull() ?: return null
        val copy = ByteBuffer.allocateDirect(source.remaining()).order(source.order())
        copy.put(source)
        copy.rewind()
        mask.close()
        return SegmentationMask(width, height, copy)
    }

    fun close() {
        runCatching { segmenter?.close() }
    }

    companion object {
        private const val MODEL_ASSET = "selfie_segmenter.tflite"
        private const val TAG = "MediaPipeSegmenter"
    }
}

internal data class SegmentationMask(
    val width: Int,
    val height: Int,
    val floatBuffer: ByteBuffer
)
