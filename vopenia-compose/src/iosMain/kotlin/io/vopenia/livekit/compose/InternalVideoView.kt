package io.vopenia.livekit.compose

import LiveKitClient.LocalVideoTrack
import LiveKitClient.createCameraTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import io.vopenia.livekit.Room
import io.vopenia.livekit.VideoViewFactory
import io.vopenia.livekit.VideoViewWrapper
import io.vopenia.livekit.participant.track.IVideoTrack
import io.vopenia.livekit.participant.track.local.LocalVideoTrackPreview
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor

@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun InternalVideoView(
    modifier: Modifier,
    room: Room? = null,
    track: IVideoTrack,
    scaleType: ScaleType,
    isMirror: Boolean,
    cornerRadius: Dp = 0.dp,
) {
    var previousTrack: IVideoTrack? by remember { mutableStateOf(null) }
    var rememberedWrapper: VideoViewWrapper? by remember { mutableStateOf(null) }

    // The tile mounts this view as soon as the track is PUBLISHED - mounting a
    // renderer is precisely what tells LiveKit the track is on screen and
    // unpauses an adaptive subscription - so the publication usually carries no
    // media track yet at that point, and there is nothing to bind the view to.
    // A media track exists once the publication is SUBSCRIBED, hence the re-run
    // below on that flag (the Android renderer keys its attach on the same one).
    // Without it a track published mid-call - a screen share, a late joiner's
    // camera - keeps a renderer-less view and stays black for the whole call.
    val isSubscribed = track.state.collectAsState().value.subscribed

    val layoutMode: Long = when (scaleType) {
        ScaleType.Fill -> 1L
        ScaleType.Fit -> 0L
    }

    val mirrorMode: Long = if (isMirror) {
        2L
    } else {
        1L
    }

    LaunchedEffect(track, isSubscribed) {
        // Nothing to attach to until the interop factory below has built the
        // view; it attaches the current track itself, and this effect re-runs
        // on the next composition anyway.
        val wrapper = rememberedWrapper ?: return@LaunchedEffect

        if (previousTrack !== track) {
            previousTrack?.let { wrapper.detach(it) }
            previousTrack = track
        }

        // Re-binding the track already bound is a no-op on the LiveKit side (the
        // native VideoView compares it with the one it holds), so attaching on
        // every re-run is free - and it is what picks up a subscription that
        // landed after the view was created.
        wrapper.attach(track)
    }

    LaunchedEffect(mirrorMode, layoutMode) {
        rememberedWrapper?.videoView?.let {
            it.setMirrorMode(mirrorMode)
            it.setLayoutMode(layoutMode)
        }
    }

    // The interop view is a hole punched through the Compose canvas: nothing
    // Compose paints behind it shows through, so a Fit (letterboxed) track
    // must carry its own dark band color or the bands show the window
    // background (white). Fill covers the whole view and keeps it transparent.
    val letterboxColor = if (scaleType == ScaleType.Fit) UIColor.blackColor else UIColor.clearColor

    // A Compose `clip` cannot round this view: it is a native view punched through the
    // Compose canvas, so it keeps square corners inside a rounded tile (visible on the
    // floating self-view, whose video overflowed the tile's rounded top). Round the
    // layer itself instead. dp and points are the same unit here.
    val radiusPoints = cornerRadius.value.toDouble()

    UIKitView(
        factory = {
            val wrapper = VideoViewFactory.createVideoView()

            rememberedWrapper = wrapper

            wrapper.attach(track)
            previousTrack = track
            wrapper.videoView.backgroundColor = letterboxColor
            wrapper.videoView.clipsToBounds = true
            wrapper.videoView.layer.cornerRadius = radiusPoints
            wrapper.videoView
        },
        modifier = modifier,
        update = {
            it.setMirrorMode(mirrorMode)
            it.setLayoutMode(layoutMode)
            it.backgroundColor = letterboxColor
            it.clipsToBounds = true
            it.layer.cornerRadius = radiusPoints
        },
        onRelease = {
            // Detach the CURRENTLY attached track, not the one captured when
            // this lambda was created: after a rebind, detaching the stale
            // track would leave the live one holding a renderer to a disposed
            // view — a leak that keeps the track pushing frames into it.
            (previousTrack ?: track).let { rememberedWrapper?.detach(it) }
        },
        // The video renderer itself is not interactive. If UIKit receives touches
        // here, taps on a participant tile don't reach the Compose clickable that
        // reveals the in-call actions overlay.
        properties = UIKitInteropProperties(
            interactionMode = null,
            isNativeAccessibilityEnabled = false,
        ),
    )
}
