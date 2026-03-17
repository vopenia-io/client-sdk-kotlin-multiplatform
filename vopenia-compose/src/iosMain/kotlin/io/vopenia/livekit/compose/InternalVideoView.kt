package io.vopenia.livekit.compose

import LiveKitClient.LocalVideoTrack
import LiveKitClient.createCameraTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import io.vopenia.livekit.Room
import io.vopenia.livekit.VideoViewFactory
import io.vopenia.livekit.VideoViewWrapper
import io.vopenia.livekit.participant.track.IVideoTrack
import io.vopenia.livekit.participant.track.local.LocalVideoTrackPreview
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun InternalVideoView(
    modifier: Modifier,
    room: Room? = null,
    track: IVideoTrack,
    scaleType: ScaleType,
    isMirror: Boolean,
) {
    var previousTrack: IVideoTrack? by remember { mutableStateOf(null) }
    var rememberedWrapper: VideoViewWrapper? by remember { mutableStateOf(null) }

    val layoutMode: Long = when (scaleType) {
        ScaleType.Fill -> 1L
        ScaleType.Fit -> 0L
    }

    val mirrorMode: Long = if (isMirror) {
        2L
    } else {
        1L
    }

    LaunchedEffect(track) {
        previousTrack?.let {
            rememberedWrapper?.detach(it)
        }

        rememberedWrapper?.attach(track)

        previousTrack = track
    }

    LaunchedEffect(mirrorMode, layoutMode) {
        rememberedWrapper?.videoView?.let {
            it.setMirrorMode(mirrorMode)
            it.setLayoutMode(layoutMode)
        }
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
            it.setMirrorMode(mirrorMode)
            it.setLayoutMode(layoutMode)
        },
        onRelease = {
            rememberedWrapper?.detach(track)
        }
    )
}
