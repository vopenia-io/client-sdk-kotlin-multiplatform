package io.vopenia.livekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import io.vopenia.livekit.effects.PreviewVideoEffect
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.video.NativeAspectCaptureFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import livekit.org.webrtc.Camera1Enumerator
import livekit.org.webrtc.Camera2Enumerator
import livekit.org.webrtc.CameraVideoCapturer
import livekit.org.webrtc.CapturerObserver
import livekit.org.webrtc.EglBase
import livekit.org.webrtc.RendererCommon
import livekit.org.webrtc.SurfaceTextureHelper
import livekit.org.webrtc.VideoFrame
import livekit.org.webrtc.VideoSink
import java.util.concurrent.atomic.AtomicReference

@Composable
actual fun CameraPreviewView(
    modifier: Modifier,
    scaleType: ScaleType,
    isMirror: Boolean,
    effect: VideoEffect?,
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

    // Preview effect pipeline (WYSIWYG prejoin). The processor is created lazily —
    // only once an effect is actually selected — because constructing it eagerly
    // loads the MediaPipe segmentation model synchronously (~1-2s), which would
    // jank the prejoin screen and waste CPU/battery for users who never pick an
    // effect (matters on low-end devices like the S9). While no processor exists,
    // frames go straight to the renderer (the original path). Held in an
    // AtomicReference so the capture-thread observer reads it without restarting
    // the capturer when the effect changes.
    val processorRef = remember { AtomicReference<PreviewVideoEffect?>(null) }

    LaunchedEffect(effect, view) {
        // Reuse an existing processor, or create one the first time an effect is
        // picked (built off the main thread — the model load is synchronous).
        val processor = processorRef.get()
            ?: if (effect != null) {
                withContext(Dispatchers.Default) { PreviewVideoEffect() }
            } else {
                null
            }
        processor?.let {
            it.setSink(view?.let { renderer -> VideoSink { frame -> renderer.onFrame(frame) } })
            it.setEffect(effect)
            // Publish only after it is fully configured so the capture-thread
            // observer never sees a half-wired processor.
            processorRef.set(it)
        }
    }

    DisposableEffect(Unit) {
        onDispose { processorRef.getAndSet(null)?.close() }
    }

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

                    override fun onFrameCaptured(frame: VideoFrame) {
                        // Route through the effect processor when present (it
                        // forwards frames unchanged while no effect is set);
                        // otherwise straight to the renderer.
                        val processor = processorRef.get()
                        if (processor != null) {
                            processor.onFrameCaptured(frame)
                        } else {
                            view?.onFrame(frame)
                        }
                    }
                }
            )

            // Capture the preview at the sensor's native aspect (like the in-call
            // track) so prejoin and the call show the same framing — not a 16:9
            // crop that looks "zoomed in". Falls back to 1280x720 if unknown.
            val nativeParams = NativeAspectCaptureFormat.compute(
                context, cameraPosition, deviceName, 720
            )
            createdCapturer.startCapture(
                nativeParams?.width ?: 1280,
                nativeParams?.height ?: 720,
                nativeParams?.maxFps ?: 30,
            )

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
