package io.vopenia.livekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.vopenia.livekit.Room
import io.vopenia.livekit.participant.track.IVideoTrack

/**
 * [cornerRadius] rounds the video itself, which a `Modifier.clip` on an ancestor cannot
 * do on iOS: the renderer is a native view punched through the Compose canvas, so it
 * keeps square corners inside a rounded tile. Pass the shape the tile uses.
 */
@Composable
expect fun VideoView(
    modifier: Modifier,
    room: Room,
    track: IVideoTrack,
    scaleType: ScaleType,
    isMirror: Boolean = false,
    cornerRadius: Dp = 0.dp,
)
