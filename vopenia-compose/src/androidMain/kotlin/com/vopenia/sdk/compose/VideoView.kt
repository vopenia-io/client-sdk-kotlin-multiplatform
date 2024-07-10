package com.vopenia.sdk.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vopenia.sdk.Room
import com.vopenia.sdk.participant.track.RemoteVideoTrack

@Composable
actual fun VideoView(
    modifier: Modifier,
    room: Room,
    track: RemoteVideoTrack,
    isMirror: Boolean,
) {

    // nothing for now
    TextureViewRendererWithProxy(
        modifier,
        room,
        isMirror = isMirror,
        track = track
    )
}
