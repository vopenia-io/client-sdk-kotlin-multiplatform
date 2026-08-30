# Copyright 2026
# SPDX-License-Identifier: GPL-3.0-or-later
#
# Android NDK build for the BigBlueBetterAudio noise-suppression library.
# Compiles RNNoise + the decoupled bbba-core + the JNI bridge into libbbba.so.
#
# The decoupled core lives in the sibling BigBlueBetterAudio repo (the workspace
# mandates a side-by-side layout). Override with -DBBBA_DIR=... if relocated.
cmake_minimum_required(VERSION 3.22.1)
project(bbba C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 14)

if(NOT DEFINED BBBA_DIR)
    set(BBBA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../BigBlueBetterAudio")
endif()
get_filename_component(BBBA_DIR "${BBBA_DIR}" ABSOLUTE)

set(RN   "${BBBA_DIR}/deps/rnnoise")
set(MD   "${BBBA_DIR}/deps/rnnoise-model-data")
set(CORE "${BBBA_DIR}/mobile/core")

if(NOT EXISTS "${RN}/src/denoise.c")
    message(FATAL_ERROR "RNNoise sources not found at ${RN}. Run: git submodule update --init deps/rnnoise (in BigBlueBetterAudio), or pass -DBBBA_DIR=")
endif()

add_library(bbba SHARED
    "${CMAKE_CURRENT_SOURCE_DIR}/bbba_jni.cpp"
    "${CORE}/bbba_core.cpp"
    "${RN}/src/celt_lpc.c"
    "${RN}/src/denoise.c"
    "${RN}/src/kiss_fft.c"
    "${RN}/src/nnet.c"
    "${RN}/src/nnet_default.c"
    "${RN}/src/parse_lpcnet_weights.c"
    "${RN}/src/pitch.c"
    "${RN}/src/rnn.c"
    "${RN}/src/rnnoise_tables.c"
    "${MD}/rnnoise_data.c"
)

target_include_directories(bbba PRIVATE "${CORE}" "${RN}/include" "${RN}/src" "${MD}")
target_compile_definitions(bbba PRIVATE RNNOISE_EXPORT= DISABLE_DEBUG_FLOAT FLOAT_APPROX)
target_compile_options(bbba PRIVATE -O3 -ffast-math)

find_library(log-lib log)
target_link_libraries(bbba ${log-lib})
