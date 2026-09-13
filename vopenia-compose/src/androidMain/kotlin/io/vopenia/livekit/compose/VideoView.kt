package io.vopenia.livekit.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import io.vopenia.livekit.Room
import io.vopenia.livekit.participant.track.IVideoTrack

@Composable
actual fun VideoView(
    modifier: Modifier,
    room: Room,
    track: IVideoTrack,
    scaleType: ScaleType,
    isMirror: Boolean,
    cornerRadius: Dp,
) {
    TextureViewRendererWithProxy(
        modifier.clip(RoundedCornerShape(cornerRadius)),
        room,
        scaleType = scaleType,
        isMirror = isMirror,
        track = track
    )
}
