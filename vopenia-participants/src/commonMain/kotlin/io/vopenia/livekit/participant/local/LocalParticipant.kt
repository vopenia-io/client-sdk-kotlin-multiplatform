package io.vopenia.livekit.participant.local

import io.vopenia.livekit.participant.Participant
import io.vopenia.livekit.participant.chat.ChatMessage
import io.vopenia.livekit.participant.devices.AudioInputDevice
import io.vopenia.livekit.participant.devices.AudioRoute
import io.vopenia.livekit.participant.devices.CameraDevice
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.track.local.LocalAudioTrack
import io.vopenia.livekit.participant.track.local.LocalTrack
import io.vopenia.livekit.participant.track.local.LocalVideoTrack
import io.vopenia.livekit.participant.video.VideoResolutionPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class LocalParticipant(scope: CoroutineScope) :
    Participant<LocalTrack, LocalParticipantState, LocalAudioTrack, LocalVideoTrack>(scope) {

    /**
     * Whether this platform can apply noise reduction to the captured
     * microphone signal. `false` on JVM (no audio backend wired up).
     *
     * UI consumers should hide the noise-reduction toggle when this is
     * `false` — [setNoiseReduction] is a no-op in that case.
     */
    open val noiseReductionSupported: Boolean = false

    protected val noiseReductionEnabledState = MutableStateFlow(true)

    /**
     * Current noise-reduction state. Default is `true` (matches the WebRTC
     * default and the historical behaviour of every consumer).
     *
     * Note on liveness: the V1 implementation uses the built-in WebRTC
     * `noiseSuppression` capture option, which is read when the microphone
     * track is created. Toggling while the mic is active therefore
     * re-publishes the audio track to take effect.
     */
    val noiseReductionEnabled: StateFlow<Boolean> = noiseReductionEnabledState.asStateFlow()

    /**
     * Enable or disable noise reduction on the captured microphone signal.
     *
     * V1 implementation: tier (a) WebRTC built-in `noiseSuppression` flag.
     * Tier (b) ML-based processor (RNNoise via BigBlueBetterAudio, or Krisp)
     * is tracked as a follow-up — see [noiseReductionSupported] and the
     * platform-specific overrides.
     *
     * No-op when [noiseReductionSupported] is `false`.
     */
    open suspend fun setNoiseReduction(enabled: Boolean) {
        noiseReductionEnabledState.value = enabled
        // Subclasses override to re-create the audio capturer with the new
        // option set if the microphone is currently active. The default
        // updates state only — used by JVM.
    }

    // ---- Audio output routing ---------------------------------------------

    protected val audioRouteState = MutableStateFlow(AudioRoute.Speaker)

    /**
     * Current in-call audio output route. Defaults to [AudioRoute.Speaker] (the
     * external loudspeaker) — the usual choice for a video call.
     */
    val audioRoute: StateFlow<AudioRoute> = audioRouteState.asStateFlow()

    protected val bluetoothConnectedState = MutableStateFlow(false)

    /**
     * Whether a Bluetooth headset is currently connected and usable for the
     * call. Drives the UI (Bluetooth glyph on the audio button) and the toggle
     * target — Bluetooth replaces the earpiece pole while connected.
     */
    val bluetoothConnected: StateFlow<Boolean> = bluetoothConnectedState.asStateFlow()

    /**
     * Route call audio to [route]. Platform implementations switch the native
     * audio session/device (output and, where applicable, mic input) and toggle
     * the proximity sensor (on only for [AudioRoute.Earpiece]). No-op on JVM.
     */
    abstract suspend fun setAudioRoute(route: AudioRoute)

    suspend fun enableMicrophone(enabled: Boolean) = enableMicrophone(enabled, null)

    suspend fun enableCamera(enabled: Boolean) = enableCamera(enabled, null)

    abstract suspend fun enableMicrophone(enabled: Boolean, device: AudioInputDevice?)

    abstract suspend fun enableCamera(enabled: Boolean, device: CameraDevice?)

    abstract suspend fun switchCamera()

    abstract suspend fun publishData(
        payload: ByteArray,
        reliable: Boolean = true,
        topic: String? = null
    )

    abstract suspend fun updateAttributes(attributes: Map<String, String>)

    abstract suspend fun sendChatMessage(text: String): ChatMessage

    /**
     * Start a screen capture and publish it as a video track. Pre-requisites
     * vary by platform:
     * - Android: the host app must obtain a MediaProjection result Intent via
     *   `MediaProjectionManager.createScreenCaptureIntent()` and register it
     *   through `io.vopenia.livekit.screenshare.ScreenShareController.setMediaProjectionResult()`
     *   before calling this method. A foreground service is required.
     * - iOS: a Broadcast Upload Extension and a shared App Group bundle id
     *   must be configured in the host Xcode project.
     * - JVM: not supported (no-op).
     */
    abstract suspend fun startScreenShare()

    /**
     * Stop the active screen capture and unpublish the screen share track.
     */
    abstract suspend fun stopScreenShare()

    /**
     * Apply (or clear) a visual effect on the local camera feed. Passing `null`
     * restores the raw camera output. Effects are mutually exclusive.
     *
     * Must be called while a camera track is active — the effect is attached
     * to the LiveKit `VideoProcessor` of the published camera track.
     *
     * Platform support:
     * - Android: requires API 24+. Backed by MediaPipe person segmentation on GPU.
     * - iOS: requires iOS 15+. Backed by Apple Vision.
     * - JVM: no-op.
     */
    abstract suspend fun setVideoEffect(effect: VideoEffect?)

    /**
     * Change the **outgoing** camera capture resolution. Applied by re-publishing
     * the camera track at the requested preset's dimensions. No-op if no camera
     * track is active. The preset persists so a track published later in the
     * call adopts it.
     *
     * Reference: Meet Web `features/settings/components/tabs/VideoTab.tsx`.
     */
    abstract suspend fun setMaxSendingResolution(preset: VideoResolutionPreset)

    // A local handle never denotes the same participant as a handle from the
    // other side of the call, so the type guard stays. The identity rule
    // itself — and hashCode — come from Participant.
    override fun equals(other: Any?): Boolean =
        other is LocalParticipant && hasSameIdentityAs(other)
}
