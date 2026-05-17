package io.vopenia.livekit.participant.devices

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import io.vopenia.livekit.Sdk
import io.vopenia.sdk.utils.Log

/**
 * Android device enumeration backed by `CameraManager` and `AudioManager`.
 * Requires the application context to be available via the SDK's
 * [HoldApplicationContext] content provider (automatic, declared in the lib's
 * AndroidManifest).
 */
actual fun availableCameras(): List<CameraDevice> {
    val context = applicationContextOrNull() ?: return defaultCameraFallback()
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        ?: return defaultCameraFallback()
    return runCatching {
        cameraManager.cameraIdList.map { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val isFront = facing == CameraCharacteristics.LENS_FACING_FRONT
            CameraDevice(
                id = id,
                label = labelFor(facing, id),
                isFront = isFront
            )
        }
    }.getOrElse {
        Log.d("availableCameras", "fallback: $it")
        defaultCameraFallback()
    }
}

actual fun availableMicrophones(): List<AudioInputDevice> {
    val context = applicationContextOrNull() ?: return defaultMicrophoneFallback()
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        ?: return defaultMicrophoneFallback()
    return runCatching {
        audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).map { info ->
            AudioInputDevice(
                id = info.id.toString(),
                label = info.productName?.toString() ?: typeLabel(info)
            )
        }
    }.getOrElse {
        Log.d("availableMicrophones", "fallback: $it")
        defaultMicrophoneFallback()
    }
}

private fun applicationContextOrNull(): Context? =
    runCatching { Sdk.applicationContext }.getOrNull()

private fun labelFor(facing: Int?, id: String): String = when (facing) {
    CameraCharacteristics.LENS_FACING_FRONT -> "Front camera"
    CameraCharacteristics.LENS_FACING_BACK -> "Back camera"
    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External camera ($id)"
    else -> "Camera $id"
}

private fun typeLabel(info: AudioDeviceInfo): String = when (info.type) {
    AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Built-in microphone"
    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth headset"
    AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset"
    AudioDeviceInfo.TYPE_USB_DEVICE -> "USB microphone"
    AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
    AudioDeviceInfo.TYPE_BLE_HEADSET ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "BLE headset" else "Audio input ${info.id}"
    else -> "Audio input ${info.id}"
}

private fun defaultCameraFallback(): List<CameraDevice> = listOf(
    CameraDevice(id = "0", label = "Front camera", isFront = true),
    CameraDevice(id = "1", label = "Back camera", isFront = false)
)

private fun defaultMicrophoneFallback(): List<AudioInputDevice> = listOf(
    AudioInputDevice(id = "default", label = "Default microphone")
)
