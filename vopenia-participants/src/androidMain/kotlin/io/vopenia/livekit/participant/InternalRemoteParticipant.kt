package io.vopenia.livekit.participant

import android.util.Log
import io.vopenia.livekit.participant.chat.ChatMessageProto
import io.vopenia.livekit.participant.chat.ChatTopics
import io.vopenia.livekit.participant.data.DataPacket
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipantState
import io.vopenia.livekit.participant.track.Kind
import io.vopenia.livekit.participant.track.RemoteAudioTrack
import io.vopenia.livekit.participant.track.RemoteNoneTrack
import io.vopenia.livekit.participant.track.RemoteTrack
import io.vopenia.livekit.participant.track.RemoteVideoTrack
import io.vopenia.livekit.participant.track.kindFrom
import io.vopenia.livekit.participant.track.toLocalTranscriptionSegment
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.collect
import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
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
        } ?: ParticipantPermissions(),
        attributes = remoteParticipant.attributes
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

    fun onConnect() {
        if (null == collection) {
            startCollect()

            scope.async {
                stateFlow.emit(stateFlow.value.copy(connected = true))
            }
        }
    }

    fun onDisconnect() {
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
                getOrCreate(it).setPublished(true)
            }
        }

        collection = scope.launch {
            remoteParticipant.events.collect {
                when (it) {
                    is ParticipantEvent.DataReceived -> {
                        handleDataReceived(it.data, it.topic, it.participant.identity?.value)
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
                        getOrCreate(it.publication as RemoteTrackPublication).setMuted(true)
                    }

                    is ParticipantEvent.TrackPublished -> {
                        Log.d("REMOTE", "published $it")
                        getOrCreate(it.publication).setPublished(true)
                    }

                    is ParticipantEvent.TrackStreamStateChanged -> {
                        it.trackPublication.let { trackPublication ->
                            if (trackPublication is RemoteTrackPublication) {
                                getOrCreate(trackPublication).let { wrapper ->
                                    wrapper.setActive(it.streamState == Track.StreamState.ACTIVE)
                                    wrapper.refreshDimensions()
                                }
                            }
                        }
                    }

                    is ParticipantEvent.TrackSubscribed -> {
                        Log.d("REMOTE", "track subscribed $it")
                        getOrCreate(it.publication).let { wrapper ->
                            wrapper.setSubscribed(true)
                            wrapper.refreshDimensions()
                        }
                    }

                    is ParticipantEvent.TrackSubscriptionFailed -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscriptionPermissionChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackUnmuted -> {
                        getOrCreate(it.publication as RemoteTrackPublication).setMuted(false)
                    }

                    is ParticipantEvent.TrackUnpublished -> {
                        Log.d("REMOTE", "unpublished $it")
                        getOrCreate(it.publication).setPublished(false)
                    }

                    is ParticipantEvent.TrackUnsubscribed -> {
                        Log.d("REMOTE", "unsubscribed $it")
                        getOrCreate(it.publication).setSubscribed(false)
                    }

                    is ParticipantEvent.AttributesChanged -> {
                        stateFlow.emit(stateFlow.value.copy(attributes = remoteParticipant.attributes))
                    }

                    is ParticipantEvent.LocalTrackPublicationFailed -> {
                        // TODO
                    }

                    is ParticipantEvent.LocalTrackSubscribed -> {
                        // TODO
                    }

                    is ParticipantEvent.StateChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TranscriptionReceived -> {
                        it.transcriptions.forEach { transcript ->
                            transcriptsFlow.emit(transcript.toLocalTranscriptionSegment())
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleDataReceived(
        data: ByteArray,
        topic: String?,
        senderIdentity: String?
    ) {
        dataReceivedFlowInternal.emit(DataPacket(data, topic, senderIdentity))
        if (topic == ChatTopics.CHAT) {
            runCatching { ChatMessageProto.decode(data, senderIdentity) }
                .getOrNull()
                ?.let { chatMessagesFlowInternal.emit(it) }
        }
    }

    /**
     * The wrapper for [track], registered on this participant. Atomic find-or-create:
     * see Participant.getOrAppend. Events reach us serialised here (one collector), but
     * the registration itself is shared with iOS, where they are not.
     */
    private fun getOrCreate(track: RemoteTrackPublication): RemoteTrack {
        val sid = track.sid
        val (wrapper, isNew) = getOrAppend(
            matches = { it.sid == sid },
            create = {
                when (kindFrom(track.kind)) {
                    Kind.Audio -> RemoteAudioTrack(scope, track)
                    Kind.Video -> RemoteVideoTrack(scope, track)
                    Kind.None -> RemoteNoneTrack(scope, track)
                }
            },
        )
        if (!isNew) wrapper.updateInternalTrack(track)
        return wrapper
    }
}
