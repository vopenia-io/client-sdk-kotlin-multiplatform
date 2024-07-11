package com.vopenia.sdk

import io.livekit.android.renderer.TextureViewRenderer

fun Room.initVideoRenderer(textureViewRenderer: TextureViewRenderer) {
    internalRoom.initVideoRenderer(textureViewRenderer)
}
