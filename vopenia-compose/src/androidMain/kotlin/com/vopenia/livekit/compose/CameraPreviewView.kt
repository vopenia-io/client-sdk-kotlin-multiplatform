package com.vopenia.livekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.CameraPosition
import livekit.org.webrtc.Camera1Enumerator
import livekit.org.webrtc.Camera2Enumerator
import livekit.org.webrtc.CameraVideoCapturer
import livekit.org.webrtc.CapturerObserver
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.RendererCommon
import livekit.org.webrtc.SurfaceTextureHelper
import livekit.org.webrtc.VideoFrame

@Composable
actual fun CameraPreviewView(
    modifier: Modifier,
    scaleType: ScaleType,
    isMirror: Boolean,
) {
    val cameraPosition = CameraPosition.FRONT
    val context = LocalContext.current

    val eglBaseContext = remember { EglBase.create().eglBaseContext }
    val cameraEnumerator = remember {
        if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator()
        }
    }

    var view: TextureViewRenderer? by remember { mutableStateOf(null) }

    DisposableEffect(cameraPosition) {
        val deviceName = cameraEnumerator
            .deviceNames
            .firstOrNull { name ->
                when (cameraPosition) {
                    CameraPosition.FRONT -> cameraEnumerator.isFrontFacing(name)
                    CameraPosition.BACK -> cameraEnumerator.isBackFacing(name)
                }
            }

        var capturer: CameraVideoCapturer? = null
        if (deviceName != null) {
            val createdCapturer = cameraEnumerator.createCapturer(
                deviceName,
                object : CameraVideoCapturer.CameraEventsHandler {
                    override fun onCameraError(p0: String?) = Unit

                    override fun onCameraDisconnected() = Unit

                    override fun onCameraFreezed(p0: String?) = Unit

                    override fun onCameraOpening(p0: String?) = Unit

                    override fun onFirstFrameAvailable() = Unit

                    override fun onCameraClosed() = Unit
                })

            val surfaceTextureHelper =
                SurfaceTextureHelper.create("VideoCaptureThread", eglBaseContext)
            createdCapturer.initialize(
                surfaceTextureHelper,
                context,
                object : CapturerObserver {
                    override fun onCapturerStarted(started: Boolean) = Unit

                    override fun onCapturerStopped() = Unit

                    override fun onFrameCaptured(frame: VideoFrame) = view?.onFrame(frame) ?: Unit
                }
            )

            createdCapturer.startCapture(1280, 720, 30)

            capturer = createdCapturer
        }

        onDispose {
            capturer?.stopCapture()
        }
    }

    DisposableEffect(view, isMirror) {
        view?.setMirror(isMirror)
        onDispose { }
    }

    DisposableEffect(currentCompositeKeyHash.toString()) {
        onDispose {
            view?.release()
        }
    }

    AndroidView(
        factory = {
            TextureViewRenderer(context).apply {
                this.init(eglBaseContext, null)
                this.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                this.setEnableHardwareScaler(false)
                view = this
            }
        },
        modifier = modifier
    )
}
