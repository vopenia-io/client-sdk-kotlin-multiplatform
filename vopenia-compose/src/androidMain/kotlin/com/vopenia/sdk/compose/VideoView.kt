package com.vopenia.sdk.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vopenia.sdk.Room
import com.vopenia.sdk.participant.track.InternalRemoteTrack
import com.vopenia.sdk.participant.track.RemoteTrack

@Composable
actual fun VideoView(
    modifier: Modifier,
    room: Room,
    track: RemoteTrack,
    isMirror: Boolean,
) {
    var previousTrack: RemoteTrack? by remember { mutableStateOf(null) }
    val proxy by remember { mutableStateOf(ProxyVideoSink()) }

    DisposableEffect(Unit) {
        onDispose {
            previousTrack?.removeRenderer(proxy)
        }
    }

    LaunchedEffect(track) {
        track.addRenderer(proxy)
        previousTrack?.removeRenderer(proxy)

        previousTrack = track
    }

    // nothing for now
    TextureViewRendererWithProxy(
        modifier,
        room,
        isMirror,
        //scalingType,
        //configuration,
        proxy = proxy
    )
}
