package io.vopenia.livekit.audio

import LiveKitClientKotlin.LocalParticipantKotlin
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Who configures and activates the iOS AVAudioSession during a call.
 *
 * LiveKit does it automatically by default, each time its audio engine starts or stops.
 * An app that reports its calls to CallKit must turn that off before the first room
 * connects, and set the session category itself: CallKit alone activates the session
 * (`provider(_:didActivate:)`), and a LiveKit audio engine started outside that window
 * fails, leaving the far end silent (LiveKit CallKit integration guide).
 */
@OptIn(ExperimentalForeignApi::class)
object AudioSessionManagement {
    fun setAutomaticConfigurationEnabled(enabled: Boolean) {
        LocalParticipantKotlin.setAutomaticAudioSessionConfigurationEnabled(enabled)
    }
}
