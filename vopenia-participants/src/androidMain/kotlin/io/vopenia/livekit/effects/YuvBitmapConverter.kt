package io.vopenia.livekit.effects

import android.graphics.Bitmap
import livekit.org.webrtc.JavaI420Buffer
import livekit.org.webrtc.VideoFrame

/**
 * I420 ↔ Bitmap conversion using BT.601 limited-range matrices.
 *
 * V1 is pure-CPU Kotlin — fine for ~720p, becomes the bottleneck at higher
 * resolutions. A future GPU path (GLES + YUV-to-RGB shader) is the optimisation
 * target. Single-threaded per call; the caller should run conversions on a
 * worker thread.
 */
internal object YuvBitmapConverter {

    /**
     * Convert an I420 buffer to an ARGB_8888 [Bitmap]. The buffer is not
     * retained — callers must call `buffer.retain()` upstream if needed.
     */
    fun i420ToBitmap(i420: VideoFrame.I420Buffer): Bitmap {
        val w = i420.width
        val h = i420.height
        val yBuf = i420.dataY
        val uBuf = i420.dataU
        val vBuf = i420.dataV
        val sy = i420.strideY
        val su = i420.strideU
        val sv = i420.strideV

        val pixels = IntArray(w * h)
        for (j in 0 until h) {
            val yRow = j * sy
            val uvRow = (j shr 1)
            val uRow = uvRow * su
            val vRow = uvRow * sv
            for (i in 0 until w) {
                val yVal = (yBuf.get(yRow + i).toInt() and 0xFF) - 16
                val uvCol = i shr 1
                val uVal = (uBuf.get(uRow + uvCol).toInt() and 0xFF) - 128
                val vVal = (vBuf.get(vRow + uvCol).toInt() and 0xFF) - 128

                val y1192 = 1192 * yVal
                var r = (y1192 + 1634 * vVal) shr 10
                var g = (y1192 - 833 * vVal - 400 * uVal) shr 10
                var b = (y1192 + 2066 * uVal) shr 10
                if (r < 0) r = 0 else if (r > 255) r = 255
                if (g < 0) g = 0 else if (g > 255) g = 255
                if (b < 0) b = 0 else if (b > 255) b = 255
                pixels[j * w + i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    /**
     * Write an ARGB_8888 [Bitmap] into a freshly allocated I420 buffer.
     * The returned buffer ownership is transferred to the caller (must
     * `release()` it). Bitmap dimensions must match the I420 dimensions
     * requested by the caller — here the buffer is allocated at the bitmap's
     * width × height.
     */
    fun bitmapToI420(bitmap: Bitmap): JavaI420Buffer {
        val w = bitmap.width
        val h = bitmap.height
        val buffer = JavaI420Buffer.allocate(w, h)
        val yPlane = buffer.dataY
        val uPlane = buffer.dataU
        val vPlane = buffer.dataV
        val sy = buffer.strideY
        val su = buffer.strideU
        val sv = buffer.strideV

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        for (j in 0 until h) {
            val yRow = j * sy
            val isEvenRow = (j and 1) == 0
            val uvRow = j shr 1
            val uRowOffset = uvRow * su
            val vRowOffset = uvRow * sv
            for (i in 0 until w) {
                val px = pixels[j * w + i]
                val r = (px shr 16) and 0xFF
                val g = (px shr 8) and 0xFF
                val b = px and 0xFF

                val yVal = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yPlane.put(yRow + i, clamp255(yVal).toByte())

                // Chroma is sub-sampled 2x2: write only at even rows AND even cols.
                if (isEvenRow && (i and 1) == 0) {
                    val uvCol = i shr 1
                    val uVal = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val vVal = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    uPlane.put(uRowOffset + uvCol, clamp255(uVal).toByte())
                    vPlane.put(vRowOffset + uvCol, clamp255(vVal).toByte())
                }
            }
        }
        return buffer
    }

    private fun clamp255(v: Int): Int = if (v < 0) 0 else if (v > 255) 255 else v
}
