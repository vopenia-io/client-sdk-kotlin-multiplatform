package com.vopenia.sdk.participant.track

import LiveKitClient.VideoView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual interface VideoSink {
    val videoView: VideoView

    fun attach(track: RemoteVideoTrack)

    fun detach(track: RemoteVideoTrack)
}
