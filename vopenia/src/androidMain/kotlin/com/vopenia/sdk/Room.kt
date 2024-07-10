package com.vopenia.sdk

import io.livekit.android.renderer.TextureViewRenderer
import livekit.org.webrtc.VideoSink

fun Room.initVideoRenderer(textureViewRenderer: TextureViewRenderer) {
    internalRoom.initVideoRenderer(textureViewRenderer)
}