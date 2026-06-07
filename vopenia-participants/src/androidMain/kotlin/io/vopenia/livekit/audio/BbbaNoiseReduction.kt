// Copyright 2026
// SPDX-License-Identifier: GPL-3.0-or-later

package io.vopenia.livekit.audio

import io.livekit.android.audio.AudioProcessorOptions

/**
 * Process-wide holder for the BigBlueBetterAudio capture-post noise processor.
 *
 * The processor must be registered when the LiveKit Room (and its
 * PeerConnectionFactory) is created — via `LiveKitOverrides → AudioOptions →
 * AudioProcessorOptions` — but it is toggled later from
 * `LocalParticipant.setNoiseReduction`. This object bridges those two call
 * sites across the `:vopenia` / `:vopenia-participants` module boundary
 * without leaking the livekit `AudioProcessorInterface` type into public API.
 *
 * V1 is a single shared processor (the SDK drives one room at a time).
 */
object BbbaNoiseReduction {
    private val processor = BbbaAudioProcessor()

    /** Register at Room creation: `AudioOptions(audioProcessorOptions = audioProcessorOptions())`. */
    fun audioProcessorOptions(): AudioProcessorOptions =
        AudioProcessorOptions(capturePostProcessor = processor)

    /** Toggle noise suppression live; pure pass-through while disabled. */
    fun setEnabled(enabled: Boolean) = processor.setEnabled(enabled)

    /** RNNoise dry/wet intensity, 0..100 (% wet). */
    fun setIntensity(percent: Float) = processor.setIntensity(percent)
}
