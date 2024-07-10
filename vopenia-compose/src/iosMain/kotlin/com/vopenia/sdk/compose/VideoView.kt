package com.vopenia.sdk.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.vopenia.sdk.Room
import com.vopenia.sdk.VideoViewFactory
import com.vopenia.sdk.VideoViewWrapper
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoView(
    modifier: Modifier,
    room: Room,
    track: RemoteVideoTrack,
    isMirror: Boolean,
) {
    var previousTrack: RemoteVideoTrack? by remember { mutableStateOf(null) }
    var rememberedWrapper: VideoViewWrapper? by remember { mutableStateOf(null) }

    LaunchedEffect(track) {
        previousTrack?.let {
            rememberedWrapper?.detach(it)
        }

        rememberedWrapper?.attach(track)

        previousTrack = track
    }

    UIKitView(
        factory = {
            val wrapper = VideoViewFactory.createVideoView()

            rememberedWrapper = wrapper

            wrapper.attach(track)
            wrapper.videoView
        },
        modifier = modifier,
        update = {
            //TODO 2L -> mirror, 1L -> OFF, 0L -> auto
            it.setMirrorMode(
                if (isMirror) {
                    2L
                } else {
                    1L
                }
            )
        },
        onRelease = {
            rememberedWrapper?.detach(track)
        }
    )
}
