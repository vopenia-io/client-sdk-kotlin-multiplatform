package com.vopenia.sdk.participant

import android.util.Log
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipantState
import com.vopenia.sdk.participant.track.InternalRemoteTrack
import com.vopenia.sdk.participant.track.RemoteTrack
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.collect
import io.livekit.android.room.track.RemoteTrackPublication
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

    private val remoteTracks = MutableStateFlow<List<InternalRemoteTrack>>(emptyList())
    override val tracks: StateFlow<List<RemoteTrack>> = remoteTracks.asStateFlow()

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
                        Log.d("REMOTE", "published $it")
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setPublished(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackStreamStateChanged -> {
                        Log.d("REMOTE", "track stream state $it")
                        //TODO
                    }

                    is ParticipantEvent.TrackSubscribed -> {
                        Log.d("REMOTE", "track subscribed $it")
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setSubscribed(true)
                        if (new) append(wrapper)
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
                        Log.d("REMOTE", "unpublished $it")
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setPublished(false)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackUnsubscribed -> {
                        Log.d("REMOTE", "unsubscribed $it")
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setSubscribed(false)
                        if (new) append(wrapper)
                    }
                }
            }
        }
    }

    private fun append(track: InternalRemoteTrack) {
        scope.launch {
            remoteTracks.emit(remoteTracks.value + track)
        }
    }

    private fun getOrCreate(
        track: RemoteTrackPublication
    ): Pair<InternalRemoteTrack, Boolean> {
        Log.d("REMOTE", "getOrCreate for ${track.sid}")

        return remoteTracks.value.find { it.sid == track.sid }.let {
            if (null != it) {
                it to false
            } else {
                InternalRemoteTrack(scope, track) to true
            }
        }
    }
}