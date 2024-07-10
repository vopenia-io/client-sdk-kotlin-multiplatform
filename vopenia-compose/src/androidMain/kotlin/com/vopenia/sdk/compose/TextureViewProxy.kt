package com.vopenia.sdk.compose


import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.vopenia.sdk.Room
import com.vopenia.sdk.initVideoRenderer
import io.livekit.android.renderer.TextureViewRenderer
import livekit.org.webrtc.RendererCommon

@Suppress("LongMethod", "ComplexMethod", "ComplexCondition")
@Composable
fun TextureViewRendererWithProxy(
    modifier: Modifier = Modifier,
    room: Room,
    isMirror: Boolean = false,
    scalingType: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT,
    //configuration: TextureViewRendererConfiguration,
    proxy: IProxyVideoSink
) {
    var androidView: TextureViewRenderer? by remember { mutableStateOf(null) }

    LaunchedEffect(proxy) {
        androidView?.let { proxy.register(it) }
    }

    LaunchedEffect(scalingType, isMirror) {
        androidView?.let {
            it.setMirror(isMirror)
            it.setScalingType(scalingType)
        }
    }

    DisposableEffect(true) {
        onDispose {
            androidView?.let {
                proxy.unregister(it)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextureViewRendererWrapper(context).let { view ->
                room.initVideoRenderer(view.child)

                view
            }
        },
        update = { parent ->
            val view = parent.child
            view.setMirror(isMirror)
            view.setScalingType(scalingType)

            if (androidView != view) {
                androidView = view
                proxy.register(view)
            }
        },
        onRelease = {
            it.release()
            proxy.unregister(it.child)
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
