package com.vopenia.livekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vopenia.livekit.Room
import com.vopenia.livekit.participant.track.IVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoView(
    modifier: Modifier,
    room: Room,
    track: IVideoTrack,
    scaleType: ScaleType,
    isMirror: Boolean,
) = InternalVideoView(
    modifier,
    room,
    track,
    scaleType,
    isMirror
)
