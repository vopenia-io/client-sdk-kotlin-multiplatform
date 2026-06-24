package io.vopenia.livekit.participant.video

import android.content.Context
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.VideoCaptureParameter
import io.vopenia.sdk.utils.Log
import livekit.org.webrtc.Camera1Enumerator
import livekit.org.webrtc.Camera2Enumerator
import livekit.org.webrtc.CameraEnumerationAndroid.CaptureFormat
import livekit.org.webrtc.CameraEnumerator
import kotlin.math.abs

/**
 * Picks a camera capture format that matches the **sensor's native aspect
 * ratio**, to stay as close to the hardware as possible.
 *
 * LiveKit's default capture is a fixed 16:9 (`VideoPreset169.H720` = 1280×720)
 * applied to every camera regardless of its sensor. Most phone sensors are
 * natively 4:3; forcing 16:9 makes the camera HAL crop the top/bottom, which
 * narrows the field of view — the camera looks "zoomed in". Capturing at the
 * sensor's native aspect avoids that crop and restores the full FOV.
 *
 * Generic, no device-specific code: the native aspect is inferred from the
 * largest supported capture format (≈ full sensor read-out) the camera reports,
 * then we pick the supported format at that aspect whose height is closest to
 * [targetHeight] (landscape coordinates, like WebRTC's capture formats).
 */
object NativeAspectCaptureFormat {

    private val formatsCache = HashMap<String, List<CaptureFormat>>()

    /**
     * @return a [VideoCaptureParameter] at the selected camera's native aspect,
     *   or null if the supported formats can't be enumerated (caller should keep
     *   the existing/default format then).
     */
    fun compute(
        context: Context,
        position: CameraPosition?,
        deviceId: String?,
        targetHeight: Int,
    ): VideoCaptureParameter? {
        val enumerator: CameraEnumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()

        val names = runCatching { enumerator.deviceNames }.getOrNull()?.toList() ?: return null
        val deviceName = deviceId?.takeIf { names.contains(it) }
            ?: position?.let { pos ->
                names.firstOrNull { name ->
                    runCatching {
                        when (pos) {
                            CameraPosition.FRONT -> enumerator.isFrontFacing(name)
                            CameraPosition.BACK -> enumerator.isBackFacing(name)
                        }
                    }.getOrDefault(false)
                }
            }
            ?: names.firstOrNull()
            ?: return null

        val formats = formatsCache.getOrPut(deviceName) {
            runCatching { enumerator.getSupportedFormats(deviceName) }.getOrNull().orEmpty()
        }.takeIf { it.isNotEmpty() } ?: return null

        // Native aspect ≈ aspect of the largest-area format (full sensor read-out).
        val native = formats.maxByOrNull { it.width.toLong() * it.height.toLong() } ?: return null
        val nativeAspect = native.width.toDouble() / native.height.toDouble()

        val sameAspect = formats.filter {
            abs(it.width.toDouble() / it.height.toDouble() - nativeAspect) <= ASPECT_EPS
        }.ifEmpty { listOf(native) }

        val best = sameAspect.minByOrNull { abs(it.height - targetHeight) } ?: native
        val fps = ((best.framerate?.max ?: DEFAULT_FPS_MILLI) / 1000).coerceIn(1, 30)

        Log.d(
            TAG,
            "native-aspect capture: dev=$deviceName ${best.width}x${best.height}@$fps " +
                "aspect=${"%.3f".format(nativeAspect)} targetH=$targetHeight"
        )
        return VideoCaptureParameter(best.width, best.height, fps)
    }

    private const val ASPECT_EPS = 0.05
    private const val DEFAULT_FPS_MILLI = 30_000
    private const val TAG = "NativeAspectCapture"
}
