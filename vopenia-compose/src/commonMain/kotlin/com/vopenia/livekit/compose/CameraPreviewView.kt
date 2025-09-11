package com.vopenia.livekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreviewView(
    modifier: Modifier,
    scaleType: ScaleType,
    isMirror: Boolean = false,
)
