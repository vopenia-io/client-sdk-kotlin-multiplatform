package io.vopenia.livekit.participant.devices

/**
 * iOS device enumeration is currently a stub returning a conventional
 * front/back pair. A real enumeration would use `AVCaptureDevice.DiscoverySession`
 * and `AVAudioSession.availableInputs` — left as a follow-up.
 */
actual fun availableCameras(): List<CameraDevice> = listOf(
    CameraDevice(id = "front", label = "Front camera", isFront = true),
    CameraDevice(id = "back", label = "Back camera", isFront = false)
)

actual fun availableMicrophones(): List<AudioInputDevice> = listOf(
    AudioInputDevice(id = "default", label = "Default microphone")
)
