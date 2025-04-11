package com.vopenia.sdk.compose

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.vopenia.sdk.Room
import com.vopenia.sdk.initVideoRenderer
import com.vopenia.sdk.participant.track.IVideoTrack
import com.vopenia.sdk.utils.Log
import io.livekit.android.renderer.TextureViewRenderer
import livekit.org.webrtc.RendererCommon

@Suppress("LongMethod", "ComplexMethod", "ComplexCondition")
@Composable
fun TextureViewRendererWithProxy(
    modifier: Modifier = Modifier,
    room: Room,
    scaleType: ScaleType,
    track: IVideoTrack,
    isMirror: Boolean,
) {
    var androidView: TextureViewRenderer? by remember { mutableStateOf(null) }
    var previousTrack: IVideoTrack? by remember { mutableStateOf(null) }

    val state by track.state.collectAsState()

    val scalingType = when (scaleType) {
        ScaleType.Fill -> RendererCommon.ScalingType.SCALE_ASPECT_FILL
        ScaleType.Fit -> RendererCommon.ScalingType.SCALE_ASPECT_FIT
    }

    LaunchedEffect(track, state.subscribed) {
        androidView?.let { view ->
            if (previousTrack != track) {
                Log.d("VideoView", "removing because previous is different")
                previousTrack?.removeRenderer(view)
            }

            if (state.subscribed) {
                Log.d("VideoView", "removing first")
                track.removeRenderer(view)
            }

            Log.d("VideoView", "attaching")
            track.addRenderer(view)

            if (previousTrack != track) {
                Log.d("VideoView", "now set the previous to the current")
                previousTrack = track
            }
        }
    }

    LaunchedEffect(scalingType, isMirror) {
        androidView?.let {
            Log.d("VideoView", "update scaling or mirror")
            it.setMirror(isMirror)
            it.setScalingType(scalingType)
        }
    }

    DisposableEffect(true) {
        onDispose {
            Log.d("VideoView", "clean up...")
            androidView?.let {
                track.removeRenderer(it)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Log.d("VideoView", "create !")
            TextureViewRendererWrapper(context).let { view ->
                room.initVideoRenderer(view.child)

                view
            }
        },
        update = { parent ->
            Log.d("VideoView", "update view")
            val view = parent.child
            view.setMirror(isMirror)
            view.setScalingType(scalingType)

            if (androidView != view) {
                androidView = view
            }
        },
        onRelease = {
            Log.d("VideoView", "release all")
            it.release()
        }
    )
}

/**
 * Simple wrapper existing so that the TextureViewRenderer can be properly manage the
 * Fit vs Fill vs Balanced views
 *
 * This is because using the view directly would be problematic with the AndroidView
 */
private class TextureViewRendererWrapper(context: Context) : FrameLayout(context) {
    val child = TextureViewRenderer(context)

    init {
        addView(
            child,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        )
    }

    fun release() = child.release()
}
