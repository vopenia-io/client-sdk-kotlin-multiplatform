package com.vopenia.sdk.participant

import android.util.Log
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipantState
import com.vopenia.sdk.participant.track.Kind
import com.vopenia.sdk.participant.track.RemoteAudioTrack
import com.vopenia.sdk.participant.track.RemoteNoneTrack
import com.vopenia.sdk.participant.track.RemoteTrack
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import com.vopenia.sdk.participant.track.kindFrom
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.collect
import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import io.livekit.android.room.participant.RemoteParticipant as RP

class InternalRemoteParticipant(
    scope: CoroutineScope,
    private val remoteParticipant: RP,
    connected: Boolean
) : RemoteParticipant(
    scope,
    RemoteParticipantState(
        connected = connected,
        name = remoteParticipant.name,
        metadata = remoteParticipant.metadata,
        permissions = remoteParticipant.permissions?.let {
            InternalParticipantPermissions(it).toMultiplatform()
        } ?: ParticipantPermissions()
    )
) {
    override fun filterListAudio(tracks: List<RemoteTrack>): List<RemoteAudioTrack> {
        return tracks.filterIsInstance<RemoteAudioTrack>()
    }

    override fun filterListVideo(tracks: List<RemoteTrack>): List<RemoteVideoTrack> {
        return tracks.filterIsInstance<RemoteVideoTrack>()
    }

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

    @Suppress("LongMethod", "ComplexMethod")
    private fun startCollect() {
        if (null != collection) return

        remoteParticipant.trackPublications.values.forEach {
            if (it is RemoteTrackPublication) {
                val (wrapper, new) = getOrCreate(it)

                wrapper.setPublished(true)
                if (new) append(wrapper)
            }
        }

        collection = scope.launch {
            remoteParticipant.events.collect {
                when (it) {
                    is ParticipantEvent.DataReceived -> {
                        // TODO
                    }

                    is ParticipantEvent.LocalTrackPublished -> {
                        // TODO
                    }

                    is ParticipantEvent.LocalTrackUnpublished -> {
                        // TODO
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
                        val (wrapper, new) = getOrCreate(it.publication as RemoteTrackPublication)

                        wrapper.setMuted(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackPublished -> {
                        Log.d("REMOTE", "published $it")
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setPublished(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackStreamStateChanged -> {
                        it.trackPublication.let { trackPublication ->
                            if (trackPublication is RemoteTrackPublication) {
                                val (wrapper, new) = getOrCreate(trackPublication)

                                wrapper.setActive(it.streamState == Track.StreamState.ACTIVE)
                                if (new) append(wrapper)
                            }
                        }
                    }

                    is ParticipantEvent.TrackSubscribed -> {
                        Log.d("REMOTE", "track subscribed $it")
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setSubscribed(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackSubscriptionFailed -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscriptionPermissionChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackUnmuted -> {
                        val (wrapper, new) = getOrCreate(it.publication as RemoteTrackPublication)

                        wrapper.setMuted(false)
                        if (new) append(wrapper)
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

    private fun getOrCreate(
        track: RemoteTrackPublication
    ): Pair<RemoteTrack, Boolean> {
        Log.d("REMOTE", "getOrCreate for ${track.sid}")

        return internalTracks.value.find { it.sid == track.sid }.let {
            if (null != it) {
                it.updateInternalTrack(track)
                it to false
            } else {
                when (kindFrom(track.kind)) {
                    Kind.Audio -> RemoteAudioTrack(scope, track)
                    Kind.Video -> RemoteVideoTrack(scope, track)
                    Kind.None -> RemoteNoneTrack(scope, track)
                } to true
            }
        }
    }
}
