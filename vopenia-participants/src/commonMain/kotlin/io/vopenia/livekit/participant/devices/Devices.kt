package io.vopenia.livekit.participant.devices

/**
 * Enumeration of capture devices available on the current platform.
 *
 * - Android: backed by `android.hardware.camera2.CameraManager` for cameras
 *   and `AudioManager.getDevices(GET_DEVICES_INPUTS)` for microphones.
 * - iOS: backed by `AVCaptureDevice.DiscoverySession` and `AVAudioSession.availableInputs`.
 * - JVM: returns a single "default" device or an empty list — multi-device
 *   capture management is not supported in this target.
 */
expect fun availableCameras(): List<CameraDevice>

expect fun availableMicrophones(): List<AudioInputDevice>
