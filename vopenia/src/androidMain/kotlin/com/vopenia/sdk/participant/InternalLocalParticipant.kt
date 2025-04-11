package com.vopenia.sdk.participant

import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.local.LocalParticipantState
import com.vopenia.sdk.participant.track.Kind
import com.vopenia.sdk.participant.track.kindFrom
import com.vopenia.sdk.participant.track.local.LocalAudioTrack
import com.vopenia.sdk.participant.track.local.LocalNoneTrack
import com.vopenia.sdk.participant.track.local.LocalTrack
import com.vopenia.sdk.participant.track.local.LocalTrackPublication
import com.vopenia.sdk.participant.track.local.LocalVideoTrack
import com.vopenia.sdk.permissions.Permission
import com.vopenia.sdk.permissions.PermissionsController
import com.vopenia.sdk.utils.Log
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.collect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import io.livekit.android.room.participant.LocalParticipant as LP

class InternalLocalParticipant(
    scope: CoroutineScope,
    private val localParticipant: LP
) : LocalParticipant(scope) {
    override val stateFlow: MutableStateFlow<LocalParticipantState> = MutableStateFlow(
        LocalParticipantState(
            permissions = localParticipant.permissions?.let {
                InternalParticipantPermissions(it).toMultiplatform()
            } ?: ParticipantPermissions()
        )
    )

    override val identity = localParticipant.identity?.value

    init {
        scope.launch {
            localParticipant.events.collect {
                when (it) {
                    is ParticipantEvent.DataReceived -> {
                        // TODO
                    }

                    is ParticipantEvent.LocalTrackPublished -> {
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setPublished(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.LocalTrackUnpublished -> {
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setPublished(false)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.MetadataChanged -> {
                        stateFlow.emit(stateFlow.value.copy(metadata = it.prevMetadata))
                    }

                    is ParticipantEvent.NameChanged -> {
                        stateFlow.emit(stateFlow.value.copy(name = it.name))
                    }

                    is ParticipantEvent.ParticipantPermissionsChanged -> {
                        it.newPermissions?.let { permissions ->
                            stateFlow.emit(
                                stateFlow.value.copy(
                                    permissions = InternalParticipantPermissions(
                                        permissions
                                    ).toMultiplatform()
                                )
                            )
                        }
                    }

                    is ParticipantEvent.SpeakingChanged -> {
                        isSpeakingFlow.emit(it.isSpeaking)
                    }

                    is ParticipantEvent.TrackMuted -> {
                        Log.d("LocalParticipant", "track is muted")
                        val (wrapper, new) = getOrCreate(it.publication as LocalTrackPublication)

                        wrapper.setMuted(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackPublished -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackStreamStateChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscribed -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscriptionFailed -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscriptionPermissionChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackUnmuted -> {
                        val (wrapper, new) = getOrCreate(it.publication as LocalTrackPublication)

                        wrapper.setMuted(false)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackUnpublished -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackUnsubscribed -> {
                        // TODO
                    }
                }
            }
        }
    }

    override suspend fun enableMicrophone(enabled: Boolean) {
        PermissionsController.checkOrProvide(Permission.RECORD_AUDIO)
        Log.d("LocalParticipant", "enableMicrophone($enabled)")
        localParticipant.setMicrophoneEnabled(enabled)
    }

    override suspend fun enableCamera(enabled: Boolean) {
        PermissionsController.checkOrProvide(Permission.CAMERA)

        localParticipant.setCameraEnabled(enabled)
    }

    override fun filterListAudio(tracks: List<LocalTrack>): List<LocalAudioTrack> {
        return tracks.filterIsInstance<LocalAudioTrack>()
    }

    override fun filterListVideo(tracks: List<LocalTrack>): List<LocalVideoTrack> {
        return tracks.filterIsInstance<LocalVideoTrack>()
    }

    private fun getOrCreate(
        track: LocalTrackPublication
    ): Pair<LocalTrack, Boolean> {
        Log.d("LOCAL", "getOrCreate for ${track.sid}")

        return internalTracks.value.find { it.sid == track.sid }.let {
            if (null != it) {
                it.updateInternalTrack(track)
                it to false
            } else {
                when (kindFrom(track.kind)) {
                    Kind.Audio -> LocalAudioTrack(scope, track)
                    Kind.Video -> LocalVideoTrack(scope, track)
                    Kind.None -> LocalNoneTrack(scope, track)
                } to true
            }
        }
    }
}
