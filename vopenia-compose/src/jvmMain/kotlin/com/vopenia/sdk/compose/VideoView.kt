package com.vopenia.sdk.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vopenia.sdk.Room
import com.vopenia.sdk.participant.track.RemoteTrack

@Composable
actual fun VideoView(
    modifier: Modifier,
    room: Room,
    track: RemoteTrack,
    isMirror: Boolean,
) {
    // nothing for now
    Column(modifier = modifier) {

    }
}
