package com.vopenia.sdk.participant

import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipantState
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.collect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.livekit.android.room.participant.RemoteParticipant as RP

class InternalRemoteParticipant(
    private val scope: CoroutineScope,
    private val remoteParticipant: RP,
    connected: Boolean
) : RemoteParticipant {
    private val stateFlow = MutableStateFlow(
        RemoteParticipantState(
            connected = connected,
            name = remoteParticipant.name,
            metadata = remoteParticipant.metadata,
            permissions = remoteParticipant.permissions?.let {
                InternalParticipantPermissions(it).toMultiplatform()
            } ?: ParticipantPermissions()
        )
    )
    private val isSpeakingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var collection: Job? = null

    init {
        startCollect()
    }

    override val identity = remoteParticipant.identity?.value

    internal fun onConnect() {
        if (null == collection) {
            startCollect()

            scope.async {
                stateFlow.emit(stateFlow.value.copy(connected = true))
            }
        }
    }

    internal fun onDisconnect() {
        collection?.let {
            it.cancel()

            scope.async {
                stateFlow.emit(stateFlow.value.copy(connected = false))
            }
        }
    }

    override val state: StateFlow<RemoteParticipantState>
        get() = stateFlow.asStateFlow()

    override val isSpeakingState: StateFlow<Boolean>
        get() = isSpeakingFlow.asStateFlow()

    private fun startCollect() {
        if (null != collection) return

        collection = scope.launch {
            remoteParticipant.events.collect {
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

}