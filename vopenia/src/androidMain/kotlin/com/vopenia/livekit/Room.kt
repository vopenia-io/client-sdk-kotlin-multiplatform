package com.vopenia.livekit

import io.livekit.android.renderer.TextureViewRenderer

fun Room.initVideoRenderer(textureViewRenderer: TextureViewRenderer) {
    internalRoom.initVideoRenderer(textureViewRenderer)
}
