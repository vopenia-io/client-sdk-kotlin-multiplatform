package com.vopenia.livekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.vopenia.livekit.participant.track.local.LocalVideoTrackPreview

@Composable
actual fun CameraPreviewView(
    modifier: Modifier,
    scaleType: ScaleType,
    isMirror: Boolean,
) {
    val track by remember { mutableStateOf(LocalVideoTrackPreview()) }

    DisposableEffect(track) {
        track.start()

        onDispose {
            track.stop()
        }
    }

    InternalVideoView(
        modifier,
        track = track,
        scaleType = scaleType,
        isMirror = isMirror
    )
}
