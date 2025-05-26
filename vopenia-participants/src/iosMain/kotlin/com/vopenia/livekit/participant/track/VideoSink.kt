package com.vopenia.livekit.participant.track

import LiveKitClient.VideoView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual interface VideoSink {
    val videoView: VideoView

    fun attach(track: IVideoTrack)

    fun detach(track: IVideoTrack)
}
