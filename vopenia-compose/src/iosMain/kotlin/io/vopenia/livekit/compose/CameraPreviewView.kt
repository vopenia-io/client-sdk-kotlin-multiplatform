package io.vopenia.livekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.track.local.LocalVideoTrackPreview

@Composable
actual fun CameraPreviewView(
    modifier: Modifier,
    scaleType: ScaleType,
    isMirror: Boolean,
    effect: VideoEffect?,
) {
    val track by remember { mutableStateOf(LocalVideoTrackPreview()) }

    DisposableEffect(track) {
        track.start()

        onDispose {
            track.stop()
        }
    }

    // Apply the selected effect to the preview capturer (re-runs on change) so the
    // prejoin preview shows the same blur/background the call will use.
    LaunchedEffect(track, effect) {
        track.setVideoEffect(effect)
    }

    InternalVideoView(
        modifier,
        track = track,
        scaleType = scaleType,
        isMirror = isMirror
    )
}
