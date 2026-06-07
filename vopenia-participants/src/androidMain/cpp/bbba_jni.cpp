// Copyright 2026
// SPDX-License-Identifier: GPL-3.0-or-later
//
// JNI bridge for the BigBlueBetterAudio mobile core on Android. Wraps the
// decoupled bbba-core C API for io.vopenia.livekit.audio.BbbaAudioProcessor.
//
// WebRTC capture-post hands us full-band mono float samples in int16 scale
// (±32768) via a direct ByteBuffer (see webrtc-sdk external_audio_processor.cc:
// audio->channels()[0], numFrames = 10ms full-band count, 480 @ 48kHz). We
// convert ±32768 -> [-1,1] in place, run the core, and convert back.

#include <jni.h>

#include "bbba_core.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_io_vopenia_livekit_audio_BbbaAudioProcessor_nativeCreate(
    JNIEnv*, jobject, jint sampleRate) {
    return reinterpret_cast<jlong>(bbba_create(static_cast<int>(sampleRate)));
}

JNIEXPORT void JNICALL
Java_io_vopenia_livekit_audio_BbbaAudioProcessor_nativeDestroy(
    JNIEnv*, jobject, jlong handle) {
    bbba_destroy(reinterpret_cast<BbbaCore*>(handle));
}

JNIEXPORT void JNICALL
Java_io_vopenia_livekit_audio_BbbaAudioProcessor_nativeSetParam(
    JNIEnv* env, jobject, jlong handle, jstring symbol, jfloat value) {
    auto* core = reinterpret_cast<BbbaCore*>(handle);
    if (core == nullptr) return;
    const char* s = env->GetStringUTFChars(symbol, nullptr);
    if (s != nullptr) {
        bbba_set_param(core, s, static_cast<float>(value));
        env->ReleaseStringUTFChars(symbol, s);
    }
}

JNIEXPORT void JNICALL
Java_io_vopenia_livekit_audio_BbbaAudioProcessor_nativeProcess(
    JNIEnv* env, jobject, jlong handle, jobject buffer, jint numFrames) {
    auto* core = reinterpret_cast<BbbaCore*>(handle);
    if (core == nullptr) return;

    float* p = static_cast<float*>(env->GetDirectBufferAddress(buffer));
    if (p == nullptr) return;

    int n = static_cast<int>(numFrames);
    const jlong capBytes = env->GetDirectBufferCapacity(buffer);
    if (capBytes > 0) {
        const int maxN = static_cast<int>(capBytes / static_cast<jlong>(sizeof(float)));
        if (n > maxN) n = maxN;
    }
    if (n <= 0) return;

    constexpr float kToUnit = 1.0f / 32768.0f;
    constexpr float kToInt16 = 32768.0f;
    for (int i = 0; i < n; ++i) p[i] *= kToUnit;   // WebRTC ±32768 -> [-1,1]
    bbba_process(core, p, p, n);                    // in-place safe
    for (int i = 0; i < n; ++i) p[i] *= kToInt16;   // [-1,1] -> WebRTC ±32768
}

}  // extern "C"
