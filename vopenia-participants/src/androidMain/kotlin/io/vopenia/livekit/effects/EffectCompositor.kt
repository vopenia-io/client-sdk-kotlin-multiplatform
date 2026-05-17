package io.vopenia.livekit.effects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import io.vopenia.livekit.Sdk
import io.vopenia.livekit.participant.effects.BackgroundImage
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.sdk.utils.Log
import java.io.InputStream
import kotlin.math.max

/**
 * Builds a composited frame for a given [VideoEffect] using the segmentation
 * mask from [MediaPipeSegmenter]. V1 uses CPU-side Bitmap+Canvas composition
 * with RenderScript blur where available — adequate for 480p–720p / 30 fps on
 * modern devices, not the long-term path (GPU shaders are the V2 target).
 */
internal class EffectCompositor {

    private val renderScript: RenderScript? = runCatching {
        @Suppress("DEPRECATION")
        RenderScript.create(Sdk.applicationContext)
    }.getOrNull()

    private val blurScript: ScriptIntrinsicBlur? = renderScript?.let {
        @Suppress("DEPRECATION")
        ScriptIntrinsicBlur.create(it, Element.U8_4(it))
    }

    private val cachedImages: MutableMap<String, Bitmap> = HashMap()

    /**
     * Returns a freshly-composited bitmap of size [frame].width × [frame].height
     * with the effect applied. The caller is responsible for recycling the
     * returned bitmap when no longer needed (or feeding it back to YUV).
     *
     * On any failure returns null — caller should publish the raw frame.
     */
    fun compose(frame: Bitmap, mask: SegmentationMask, effect: VideoEffect): Bitmap? {
        val width = frame.width
        val height = frame.height
        if (width <= 0 || height <= 0) return null

        val alphaMask = maskToAlphaBitmap(mask, width, height) ?: return null
        val background: Bitmap = when (effect) {
            is VideoEffect.BlurLight -> blurredCopy(frame, radius = 12f)
            is VideoEffect.BlurStrong -> blurredCopy(frame, radius = 25f)
            is VideoEffect.Background -> resolveBackgroundImage(effect.image, width, height)
        } ?: run {
            alphaMask.recycle()
            return null
        }

        return runCatching {
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            // Background first (full screen).
            canvas.drawBitmap(background, null, Rect(0, 0, width, height), null)

            // Masked foreground: copy frame, intersect alpha with mask, draw on top.
            val maskedForeground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val fgCanvas = Canvas(maskedForeground)
            fgCanvas.drawBitmap(frame, 0f, 0f, null)
            fgCanvas.drawBitmap(
                alphaMask,
                0f,
                0f,
                Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) }
            )
            canvas.drawBitmap(maskedForeground, 0f, 0f, null)
            maskedForeground.recycle()
            alphaMask.recycle()
            output
        }.onFailure {
            Log.d(TAG, "compose failed: $it")
            alphaMask.recycle()
        }.getOrNull()
    }

    private fun blurredCopy(source: Bitmap, radius: Float): Bitmap? {
        val rs = renderScript
        val script = blurScript
        if (rs != null && script != null) {
            runCatching {
                val out = Bitmap.createBitmap(
                    source.width,
                    source.height,
                    source.config ?: Bitmap.Config.ARGB_8888
                )
                @Suppress("DEPRECATION")
                val inAlloc = Allocation.createFromBitmap(rs, source)
                @Suppress("DEPRECATION")
                val outAlloc = Allocation.createFromBitmap(rs, out)
                script.setRadius(max(1f, radius.coerceAtMost(25f)))
                script.setInput(inAlloc)
                script.forEach(outAlloc)
                outAlloc.copyTo(out)
                inAlloc.destroy()
                outAlloc.destroy()
                return out
            }.onFailure { Log.d(TAG, "RS blur failed: $it") }
        }
        return cpuBoxBlurFallback(source, radius)
    }

    private fun cpuBoxBlurFallback(source: Bitmap, radius: Float): Bitmap {
        val factor = (radius / 4f).coerceAtLeast(1f).toInt()
        val w = max(2, source.width / factor)
        val h = max(2, source.height / factor)
        val small = Bitmap.createScaledBitmap(source, w, h, true)
        val out = Bitmap.createScaledBitmap(small, source.width, source.height, true)
        if (small !== source && small !== out) small.recycle()
        return out
    }

    private fun resolveBackgroundImage(image: BackgroundImage, width: Int, height: Int): Bitmap? {
        val key = when (image) {
            is BackgroundImage.Uri -> "uri:${image.uri}"
            is BackgroundImage.Bundled -> "asset:${image.name}"
        }
        cachedImages[key]?.let { cached ->
            if (cached.width == width && cached.height == height) return cached
        }
        val raw = decodeBackground(image) ?: return null
        val scaled = Bitmap.createScaledBitmap(raw, width, height, true)
        if (scaled !== raw) raw.recycle()
        cachedImages[key]?.recycle()
        cachedImages[key] = scaled
        return scaled
    }

    private fun decodeBackground(image: BackgroundImage): Bitmap? {
        val context: Context = Sdk.applicationContext
        return runCatching {
            when (image) {
                is BackgroundImage.Bundled ->
                    context.assets.open(image.name).use(BitmapFactory::decodeStream)
                is BackgroundImage.Uri -> {
                    val uri = Uri.parse(image.uri)
                    when (uri.scheme) {
                        "file" -> uri.path?.let { BitmapFactory.decodeFile(it) }
                        "asset" -> {
                            val name = uri.path?.trimStart('/') ?: return null
                            context.assets.open(name).use(BitmapFactory::decodeStream)
                        }
                        else -> {
                            val stream: InputStream? = context.contentResolver.openInputStream(uri)
                            stream?.use { BitmapFactory.decodeStream(it) }
                        }
                    }
                }
            }
        }.onFailure { Log.d(TAG, "decodeBackground failed: $it") }
            .getOrNull()
    }

    private fun maskToAlphaBitmap(mask: SegmentationMask, targetWidth: Int, targetHeight: Int): Bitmap? {
        val maskWidth = mask.width
        val maskHeight = mask.height
        if (maskWidth <= 0 || maskHeight <= 0) return null
        val pixels = IntArray(maskWidth * maskHeight)
        val floats = mask.floatBuffer.asFloatBuffer()
        for (i in 0 until maskWidth * maskHeight) {
            val confidence = floats.get(i).coerceIn(0f, 1f)
            val alpha = (confidence * 255f).toInt() and 0xFF
            pixels[i] = (alpha shl 24) or 0x00FFFFFF
        }
        val small = Bitmap.createBitmap(pixels, maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
        if (small.width == targetWidth && small.height == targetHeight) return small
        val scaled = Bitmap.createScaledBitmap(small, targetWidth, targetHeight, true)
        if (scaled !== small) small.recycle()
        return scaled
    }

    fun close() {
        cachedImages.values.forEach { it.recycle() }
        cachedImages.clear()
        runCatching { blurScript?.destroy() }
        runCatching { renderScript?.destroy() }
    }

    companion object {
        private const val TAG = "EffectCompositor"
    }
}
