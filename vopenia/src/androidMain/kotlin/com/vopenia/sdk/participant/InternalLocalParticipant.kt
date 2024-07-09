package com.vopenia.sdk.participant

import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.local.LocalParticipantState
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.collect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.livekit.android.room.participant.LocalParticipant as LP

class InternalLocalParticipant(
    private val scope: CoroutineScope,
    private val localParticipant: LP
) : LocalParticipant {
    private val stateFlow: MutableStateFlow<LocalParticipantState> = MutableStateFlow(
        LocalParticipantState(
            permissions = localParticipant.permissions?.let {
                InternalParticipantPermissions(it).toMultiplatform()
            } ?: ParticipantPermissions()
        )
    )

    override val identity = localParticipant.identity?.value

    private val isSpeakingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        scope.launch {
            localParticipant.events.collect {
                when (it) {
                    is ParticipantEvent.DataReceived -> {
                        //TODO
                    }

                    is ParticipantEvent.LocalTrackPublished -> {
                        //TODO
                    }

                    is ParticipantEvent.LocalTrackUnpublished -> {
                        //TODO
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
                        //TODO
                    }

                    is ParticipantEvent.TrackPublished -> {
                        //TODO
                    }

                    is ParticipantEvent.TrackStreamStateChanged -> {
                        //TODO
                    }

                    is ParticipantEvent.TrackSubscribed -> {
                        //TODO
                    }

                    is ParticipantEvent.TrackSubscriptionFailed -> {
                        //TODO
                    }

                    is ParticipantEvent.TrackSubscriptionPermissionChanged -> {
                        //TODO
                    }

                    is ParticipantEvent.TrackUnmuted -> {
                        //TODO
                    }

                    is ParticipantEvent.TrackUnpublished -> {
                        //TODO
                    }

                    is ParticipantEvent.TrackUnsubscribed -> {
                        //TODO
                    }
                }
            }
        }
    }

    override val state: StateFlow<LocalParticipantState>
        get() = stateFlow.asStateFlow()

    override val isSpeakingState: StateFlow<Boolean>
        get() = isSpeakingFlow.asStateFlow()

    override suspend fun enableMicrophone(enabled: Boolean) {
        localParticipant.setMicrophoneEnabled(enabled)
    }

}