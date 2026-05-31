package io.vopenia.livekit.participant.devices

/**
 * iOS device enumeration stub. AVAudioSession.availableInputs is not exposed
 * by Kotlin/Native's `platform.AVFAudio` binding — adding a real enumeration
 * requires a small Swift helper (TODO: extend LiveKitClientKotlin wrapper
 * with `availableAudioInputs() -> [(uid: String, name: String)]`). For now
 * we return a conventional default; routing via
 * `LocalParticipant.enableMicrophone(enabled, device)` works once you have
 * the uid by other means (e.g. observing AVAudioSession route changes).
 *
 * Camera enumeration is a conventional front/back pair; the LiveKit iOS
 * camera capturer accepts only an `AVCaptureDevice.Position`, not arbitrary
 * deviceIds. A real `AVCaptureDevice.DiscoverySession` query is left as a
 * follow-up.
 */
actual fun availableCameras(): List<CameraDevice> = listOf(
    CameraDevice(id = "front", label = "Front camera", isFront = true),
    CameraDevice(id = "back", label = "Back camera", isFront = false)
)

actual fun availableMicrophones(): List<AudioInputDevice> = listOf(
    AudioInputDevice(id = "default", label = "Default microphone")
)
