package com.vopenia.sdk.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vopenia.sdk.Room
import com.vopenia.sdk.participant.track.IVideoTrack

@Composable
expect fun VideoView(
    modifier: Modifier,
    room: Room,
    track: IVideoTrack,
    scaleType: ScaleType,
    isMirror: Boolean = false,
)
