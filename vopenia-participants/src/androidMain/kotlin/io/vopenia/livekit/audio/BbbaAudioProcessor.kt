// Copyright 2026
// SPDX-License-Identifier: GPL-3.0-or-later

package io.vopenia.livekit.audio

import android.util.Log
import io.livekit.android.audio.AudioProcessorInterface
import java.nio.ByteBuffer

/**
 * LiveKit capture-post audio processor backed by the BigBlueBetterAudio native
 * core (RNNoise voice isolation + VAD gate + Faust aesthetic chain).
 *
 * Registered once at Room creation via
 * `LiveKitOverrides(audioOptions = AudioOptions(audioProcessorOptions = ...))`.
 * Toggled live through [setEnabled] (wired to `LocalParticipant.setNoiseReduction`);
 * while disabled it is a pure pass-through.
 *
 * WebRTC delivers full-band mono float frames in int16 scale (±32768); the
 * native bridge handles the ±32768 ⇄ [-1,1] conversion. RNNoise requires
 * 48 kHz — other rates are bypassed in V1 (resampling is a follow-up).
 */
internal class BbbaAudioProcessor : AudioProcessorInterface {

    @Volatile private var handle: Long = 0L
    // Default true to match `LocalParticipant.noiseReductionEnabledState`
    // (Kotlin commonMain) which is initialized to `true`. If the native side
    // stays false here, the toggle shows ON in the UI but BBBA stays OFF
    // until the user manually flips it — confusing discrepancy.
    @Volatile private var enabled: Boolean = true
    // Parameter defaults — must match the BBBA web reference UI
    // (BigBlueBetterAudio/web/index.html sliders). The bbba-core / Faust DSP
    // ships with weaker defaults (mb_strength=60, sb_strength=60,
    // leveler_target=-18 dB), which produces a noticeably quieter aesthetic
    // chain than what BBBA web users hear. Push the web defaults
    // explicitly when the native handle is created so mobile sounds identical.
    @Volatile private var intensity: Float = 100f
    @Volatile private var mbStrength: Float = 100f
    @Volatile private var sbStrength: Float = 100f
    @Volatile private var levelerTarget: Float = -23f  // dB
    private val lock = Any()

    /** Enable/disable live (pass-through when disabled). */
    fun setEnabled(value: Boolean) {
        val previous = enabled
        enabled = value
        // INFO so it's visible at default logcat level — used to confirm at
        // a glance that the UI toggle actually reaches the native processor.
        Log.i(
            TAG,
            "BBBA ${if (value) "ON " else "OFF"}" +
                " (was=$previous, handle=$handle, initialized=${handle != 0L})"
        )
    }

    /** RNNoise dry/wet, 0..100 (% wet). */
    fun setIntensity(value: Float) {
        intensity = value
        synchronized(lock) {
            if (handle != 0L) nativeSetParam(handle, "intensity", value)
        }
        Log.d(TAG, "setIntensity($value)")
    }

    // --- AudioProcessorInterface ---------------------------------------------

    override fun isEnabled(): Boolean = enabled

    override fun getName(): String = "BigBlueBetterAudio"

    override fun initializeAudioProcessing(sampleRateHz: Int, numChannels: Int) {
        synchronized(lock) {
            ensureLibraryLoaded()
            if (sampleRateHz != REQUIRED_SAMPLE_RATE) {
                Log.w(
                    TAG,
                    "BBBA bypassed: RNNoise requires ${REQUIRED_SAMPLE_RATE}Hz, " +
                        "got ${sampleRateHz}Hz"
                )
                releaseLocked()
                return
            }
            if (handle == 0L) {
                handle = nativeCreate(sampleRateHz)
                if (handle != 0L) {
                    // Push the BBBA web reference parameters explicitly. The
                    // Faust DSP's bare defaults are weaker (mb=60, sb=60,
                    // leveler=-18) — without these overrides the aesthetic
                    // chain barely lifts the post-RNNoise signal and users
                    // perceive almost no difference vs the raw mic.
                    nativeSetParam(handle, "intensity", intensity)
                    nativeSetParam(handle, "mb_strength", mbStrength)
                    nativeSetParam(handle, "sb_strength", sbStrength)
                    nativeSetParam(handle, "leveler_target", levelerTarget)
                }
                Log.i(
                    TAG,
                    "BBBA initialized @ ${sampleRateHz}Hz × $numChannels ch " +
                        "(handle=$handle, intensity=$intensity, " +
                        "mb=$mbStrength, sb=$sbStrength, " +
                        "leveler=${levelerTarget}dB, enabled=$enabled)"
                )
            } else {
                Log.d(
                    TAG,
                    "initializeAudioProcessing — already initialized " +
                        "(handle=$handle), no-op"
                )
            }
        }
    }

    override fun resetAudioProcessing(newRate: Int) {
        Log.d(TAG, "resetAudioProcessing(newRate=$newRate, oldHandle=$handle)")
        synchronized(lock) {
            releaseLocked()
        }
        initializeAudioProcessing(newRate, 1)
    }

    override fun processAudio(numBands: Int, numFrames: Int, buffer: ByteBuffer) {
        // Hot path: avoid the lock when bypassed. `handle` is volatile; a torn
        // toggle at worst skips/processes one 10ms frame, which is harmless.
        if (!enabled) return
        val h = handle
        if (h == 0L) return
        nativeProcess(h, buffer, numFrames)
        // Lightweight liveness heartbeat — every 500 frames (~5s @ 48k/10ms)
        // we know the processor is being called with audio. DEBUG so it stays
        // off by default.
        val n = frameCount + 1
        frameCount = n
        if (n % 500 == 0) {
            Log.d(TAG, "processAudio heartbeat: frames=$n numBands=$numBands numFrames=$numFrames")
        }
    }

    @Volatile private var frameCount: Int = 0

    private fun releaseLocked() {
        if (handle != 0L) {
            nativeDestroy(handle)
            handle = 0L
        }
    }

    private external fun nativeCreate(sampleRate: Int): Long
    private external fun nativeDestroy(handle: Long)
    private external fun nativeSetParam(handle: Long, symbol: String, value: Float)
    private external fun nativeProcess(handle: Long, buffer: ByteBuffer, numFrames: Int)

    companion object {
        private const val TAG = "BbbaAudioProcessor"
        private const val REQUIRED_SAMPLE_RATE = 48000

        @Volatile private var libraryLoaded = false

        private fun ensureLibraryLoaded() {
            if (!libraryLoaded) {
                System.loadLibrary("bbba")
                libraryLoaded = true
            }
        }
    }
}
