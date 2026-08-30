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
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.ceil
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
        val raw = decodeBackground(image, width, height) ?: return null
        val scaled = coverScale(raw, width, height)
        if (scaled !== raw) raw.recycle()
        cachedImages[key]?.recycle()
        cachedImages[key] = scaled
        return scaled
    }

    /**
     * Scales [src] to fill [width] x [height] while preserving its aspect ratio,
     * then center-crops the overflow ("cover"). A plain `createScaledBitmap` to the
     * frame size stretches the photo to the frame's aspect (e.g. a 16:9 background
     * squashed into a 9:16 portrait frame), which is what made the background look
     * distorted.
     */
    private fun coverScale(src: Bitmap, width: Int, height: Int): Bitmap {
        if (src.width == width && src.height == height) return src
        val scale = max(width.toFloat() / src.width, height.toFloat() / src.height)
        val sw = max(width, ceil(src.width * scale).toInt())
        val sh = max(height, ceil(src.height * scale).toInt())
        val scaled = Bitmap.createScaledBitmap(src, sw, sh, true)
        val x = (sw - width) / 2
        val y = (sh - height) / 2
        val cropped = Bitmap.createBitmap(scaled, x, y, width, height)
        if (scaled !== src && scaled !== cropped) scaled.recycle()
        return cropped
    }

    /** Opens the raw byte stream backing [image], or null if unresolvable. */
    private fun openBackgroundStream(image: BackgroundImage): InputStream? {
        val context: Context = Sdk.applicationContext
        return when (image) {
            is BackgroundImage.Bundled -> context.assets.open(image.name)
            is BackgroundImage.Uri -> {
                val uri = Uri.parse(image.uri)
                when (uri.scheme) {
                    "file" -> uri.path?.let { FileInputStream(it) }
                    "asset" -> uri.path?.trimStart('/')?.let { context.assets.open(it) }
                    else -> context.contentResolver.openInputStream(uri)
                }
            }
        }
    }

    /**
     * Decodes [image] downsampled toward [targetWidth] x [targetHeight] via
     * `inSampleSize`, so a multi-megapixel source never materialises at full
     * resolution. The bundled backgrounds are ~9 MP JPEGs (~36 MB as
     * ARGB_8888) — decoding them raw is an OOM risk on low-RAM devices, and the
     * scale-down happens just outside the compose `runCatching`, so it would
     * crash the WebRTC capture thread rather than fall back to the raw frame.
     * Two passes: bounds first, then the sub-sampled decode.
     */
    private fun decodeBackground(image: BackgroundImage, targetWidth: Int, targetHeight: Int): Bitmap? =
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openBackgroundStream(image)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = computeSampleSize(bounds.outWidth, bounds.outHeight, targetWidth, targetHeight)
            }
            openBackgroundStream(image)?.use { BitmapFactory.decodeStream(it, null, opts) }
        }.onFailure { Log.d(TAG, "decodeBackground failed: $it") }
            .getOrNull()

    /** Largest power-of-two sub-sample that stays >= the target dimensions. */
    private fun computeSampleSize(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Int {
        if (srcW <= 0 || srcH <= 0 || dstW <= 0 || dstH <= 0) return 1
        var sample = 1
        while (srcW / (sample * 2) >= dstW && srcH / (sample * 2) >= dstH) sample *= 2
        return sample
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
