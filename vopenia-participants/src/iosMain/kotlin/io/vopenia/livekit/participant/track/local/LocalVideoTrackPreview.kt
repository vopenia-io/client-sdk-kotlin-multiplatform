package io.vopenia.livekit.participant.track.local

import LiveKitClient.LocalVideoTrack
import LiveKitClient.createCameraTrack
import LiveKitClientKotlin.LocalParticipantKotlin
import LiveKitClientKotlin.VideoTrackAddKotlin
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.effects.loadBackgroundUiImage
import io.vopenia.livekit.participant.track.IVideoTrack
import io.vopenia.livekit.participant.track.TrackState
import io.vopenia.livekit.participant.track.VideoSink
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class LocalVideoTrackPreview() : IVideoTrack {
    private val track = LocalVideoTrack.createCameraTrack()

    private val delegate = VideoTrackAddKotlin()

    fun start() {
        track.startWithCompletionHandler { error ->
            println("error : ${error?.localizedDescription}")
        }
    }

    fun stop() {
        track.stopWithCompletionHandler { error ->
            println("error : ${error?.localizedDescription}")
        }
    }

    /**
     * Apply a [VideoEffect] (blur / background image) to this standalone preview
     * track so the prejoin preview is WYSIWYG. Best-effort: a failure is logged
     * and swallowed so the preview keeps rendering. Mirrors the published-track
     * path in [io.vopenia.livekit.participant.InternalLocalParticipant.setVideoEffect],
     * but targets the track directly (no participant / publication on prejoin).
     */
    suspend fun setVideoEffect(effect: VideoEffect?) {
        when (effect) {
            null, VideoEffect.BlurLight, VideoEffect.BlurStrong -> {
                val enable = effect != null
                suspendCoroutine { continuation ->
                    LocalParticipantKotlin.setBackgroundBlurWithTrack(
                        track = track,
                        enabled = enable,
                    ) { error ->
                        if (error != null) {
                            println("preview setBackgroundBlur error: ${error.localizedDescription}")
                        }
                        continuation.resume(Unit)
                    }
                }
            }

            is VideoEffect.Background -> {
                val uiImage = loadBackgroundUiImage(effect.image)
                if (uiImage == null) {
                    println("preview setVideoEffect: failed to load background image ${effect.image}")
                    return
                }
                suspendCoroutine { continuation ->
                    LocalParticipantKotlin.setBackgroundImageWithTrack(
                        track = track,
                        image = uiImage,
                    ) { error ->
                        if (error != null) {
                            println("preview setBackgroundImage error: ${error.localizedDescription}")
                        }
                        continuation.resume(Unit)
                    }
                }
            }
        }
    }

    override fun addRenderer(videoSink: VideoSink) {
        delegate.setTrackWithVideoView(videoSink.videoView, track)
    }

    override fun removeRenderer(videoSink: VideoSink) {
        delegate.removeWithVideoView(videoSink.videoView)
    }

    override val state = MutableStateFlow(
        TrackState(
            subscribed = true,
            published = false,
            active = true,
            muted = false
        )
    )
}
