package com.vopenia.sdk.participant

import LiveKitClient.addDelegate
import LiveKitClient.setCameraWithEnabled
import LiveKitClient.setMicrophoneWithEnabled
import com.vopenia.sdk.NSErrorException
import com.vopenia.sdk.participant.delegate.LocalParticipantDelegate
import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.local.LocalParticipantState
import com.vopenia.sdk.participant.track.Kind
import com.vopenia.sdk.participant.track.RemoteAudioTrack
import com.vopenia.sdk.participant.track.RemoteTrack
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import com.vopenia.sdk.participant.track.kindFrom
import com.vopenia.sdk.participant.track.local.LocalAudioTrack
import com.vopenia.sdk.participant.track.local.LocalNoneTrack
import com.vopenia.sdk.participant.track.local.LocalTrack
import com.vopenia.sdk.participant.track.local.LocalTrackPublication
import com.vopenia.sdk.participant.track.local.LocalVideoTrack
import com.vopenia.sdk.permissions.Permission
import com.vopenia.sdk.permissions.PermissionRefused
import com.vopenia.sdk.permissions.PermissionUnrecoverable
import com.vopenia.sdk.permissions.PermissionsController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import LiveKitClient.LocalParticipant as LP

@OptIn(ExperimentalForeignApi::class)
class InternalLocalParticipant(
    scope: CoroutineScope,
    private val localParticipant: LP
) : LocalParticipant(scope) {
    override val stateFlow: MutableStateFlow<LocalParticipantState> = MutableStateFlow(
        LocalParticipantState(
            permissions = InternalParticipantPermissions(
                localParticipant.permissions()
            ).toMultiplatform()
        )
    )

    override val identity = localParticipant.identity()?.stringValue()

    private val delegate = localParticipant.wrapDelegateWithDelegate(
        LocalParticipantDelegate(
            onConnectionQuality = { connectionQuality ->
                // scope.async {
                //
                // }
            },
            onIsSpeaking = { isSpeaking ->
                println("isSpeaking $isSpeaking")
                scope.async {
                    isSpeakingFlow.emit(isSpeaking)
                }
            },
            onMetadataUpdated = { metadata ->
                println("metadata $metadata")
                scope.async {
                    stateFlow.emit(stateFlow.value.copy(metadata = metadata))
                }
            },
            onNameUpdated = { name ->
                println("name $name")
                scope.async {
                    stateFlow.emit(stateFlow.value.copy(name = name))
                }
            },
            onPermissionsUpdated = { permissions ->
                println("permissions $permissions")
                scope.async {
                    stateFlow.emit(
                        stateFlow.value.copy(
                            permissions = InternalParticipantPermissions(
                                permissions
                            ).toMultiplatform()
                        )
                    )
                }
            },
            onTrackPublished = { track ->
                println("onTrackPublished $track")
                val (wrapper, new) = getOrCreate(track as LocalTrackPublication)

                wrapper.setPublished(true)
                if (new) append(wrapper)
            },
            onTrackUnpublished = { track ->
                println("onTrackUnpublished $track")
                val (wrapper, new) = getOrCreate(track as LocalTrackPublication)

                wrapper.setPublished(false)
                if (new) append(wrapper)
            },
            onTrackPublicationIsMuted = { track, isMuted ->
                val (wrapper, new) = getOrCreate(track as LocalTrackPublication)

                wrapper.setMuted(isMuted)
                if (new) append(wrapper)
            }
        )
    )

    init {
        localParticipant.addDelegate(delegate)
    }

    override fun filterListAudio(tracks: List<LocalTrack>): List<LocalAudioTrack> {
        return tracks.filterIsInstance<LocalAudioTrack>()
    }

    override fun filterListVideo(tracks: List<LocalTrack>): List<LocalVideoTrack> {
        return tracks.filterIsInstance<LocalVideoTrack>()
    }

    override suspend fun enableMicrophone(enabled: Boolean) {
        PermissionsController.checkOrProvide(Permission.RECORD_AUDIO)
        println("enableMicrophone $enabled")
        suspendCoroutine { continuation ->
            localParticipant.setMicrophoneWithEnabled(enabled, null, null) { _, error ->
                // todo manage here
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    override suspend fun enableCamera(enabled: Boolean) {
        PermissionsController.checkOrProvide(Permission.CAMERA)

        suspendCoroutine { continuation ->
            localParticipant.setCameraWithEnabled(enabled, null, null) { _, error ->
                // todo manage here
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }


    private fun getOrCreate(
        track: LocalTrackPublication
    ): Pair<LocalTrack, Boolean> {
        return internalTracks.value.find { it.sid == track.sid().stringValue()}.let {
            if (null != it) {
                it.updateInternalTrack(track)
                it to false
            } else {
                when (kindFrom(track.kind())) {
                    Kind.Audio -> LocalAudioTrack(scope, track)
                    Kind.Video -> LocalVideoTrack(scope, track)
                    Kind.None -> LocalNoneTrack(scope, track)
                } to true
            }
        }
    }
}
