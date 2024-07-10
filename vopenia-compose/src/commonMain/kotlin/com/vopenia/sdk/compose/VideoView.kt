package com.vopenia.sdk.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vopenia.sdk.Room
import com.vopenia.sdk.participant.track.RemoteTrack
import com.vopenia.sdk.participant.track.RemoteVideoTrack

@Composable
expect fun VideoView(
    modifier: Modifier,
    room: Room,
    track: RemoteVideoTrack,
    isMirror: Boolean = false,
)
