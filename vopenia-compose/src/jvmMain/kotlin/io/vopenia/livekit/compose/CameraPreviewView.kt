package io.vopenia.livekit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.vopenia.livekit.participant.effects.VideoEffect

@Composable
actual fun CameraPreviewView(
    modifier: Modifier,
    scaleType: ScaleType,
    isMirror: Boolean,
    effect: VideoEffect?,
) {
    Column(modifier = modifier) {
        // nothing for now (no JVM camera preview; effect is ignored)
    }
}
