package io.vopenia.livekit.participant

import LiveKitClient.removeDelegate
import LiveKitClientKotlin.DelegateKotlin
import io.vopenia.livekit.participant.chat.ChatMessageProto
import io.vopenia.livekit.participant.chat.ChatTopics
import io.vopenia.livekit.participant.data.DataPacket
import io.vopenia.livekit.participant.delegate.RemoteParticipantDelegate
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipantState
import io.vopenia.livekit.participant.track.Kind
import io.vopenia.livekit.participant.track.RemoteAudioTrack
import io.vopenia.livekit.participant.track.RemoteNoneTrack
import io.vopenia.livekit.participant.track.RemoteTrack
import io.vopenia.livekit.participant.track.RemoteTrackPublication
import io.vopenia.livekit.participant.track.RemoteVideoTrack
import io.vopenia.livekit.participant.track.StreamState
import io.vopenia.livekit.participant.track.kindFrom
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import LiveKitClient.RemoteParticipant as RP

@OptIn(ExperimentalForeignApi::class)
class InternalRemoteParticipant(
    scope: CoroutineScope,
    private val remoteParticipant: RP,
    connected: Boolean
) : RemoteParticipant(
    scope,
    RemoteParticipantState(
        connected = connected,
        name = remoteParticipant.name(),
        metadata = remoteParticipant.metadata(),
        permissions = InternalParticipantPermissions(
            remoteParticipant.permissions()
        ).toMultiplatform(),
        attributes = remoteParticipant.attributes() as? Map<String, String> ?: emptyMap()
    )
) {
    private val delegateWrapper = DelegateKotlin()
    private var isAttached = false

    override fun filterListAudio(tracks: List<RemoteTrack>): List<RemoteAudioTrack> {
        return tracks.filterIsInstance<RemoteAudioTrack>()
    }

    override fun filterListVideo(tracks: List<RemoteTrack>): List<RemoteVideoTrack> {
        return tracks.filterIsInstance<RemoteVideoTrack>()
    }

    override val identity = remoteParticipant.identity()?.stringValue()

    private val delegate = delegateWrapper.wrapParticipantDelegateWithDelegate(
        RemoteParticipantDelegate(
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
                getOrCreate(track).setPublished(true)
            },
            onTrackUnpublished = { track ->
                getOrCreate(track).setPublished(false)
            },
            onTrackPublicationIsMuted = { track, isMuted ->
                getOrCreate(track as RemoteTrackPublication).setMuted(isMuted)
            },
            onTrackSubscribed = { track ->
                getOrCreate(track).let { wrapper ->
                    wrapper.setSubscribed(true)
                    wrapper.refreshDimensions()
                }
            },
            onTrackUnsubscribed = { track ->
                getOrCreate(track).setSubscribed(false)
            },
            onTrackStreamStateChanged = { trackPublication, streamState ->
                getOrCreate(trackPublication).let { wrapper ->
                    wrapper.setActive(streamState == StreamState.Active)
                    wrapper.refreshDimensions()
                }
            },
            onAttributesUpdated = { attributes ->
                scope.async {
                    stateFlow.emit(stateFlow.value.copy(attributes = attributes))
                }
            },
            onDataReceived = { data, topic, senderIdentity ->
                scope.async {
                    val bytes = data.toByteArray()
                    dataReceivedFlowInternal.emit(DataPacket(bytes, topic, senderIdentity))
                    if (topic == ChatTopics.CHAT) {
                        runCatching { ChatMessageProto.decode(bytes, senderIdentity) }
                            .getOrNull()
                            ?.let { chatMessagesFlowInternal.emit(it) }
                    }
                }
            }
        )
    )

    fun onConnect() {
        if (isAttached) return

        remoteParticipant.trackPublications().values.forEach {
            if (it is RemoteTrackPublication) {
                getOrCreate(it).setPublished(true)
            }
        }

        println("added the delegate to the remote participant")
        delegateWrapper.appendToParticipant(remoteParticipant, delegate)
        isAttached = true

        scope.async {
            stateFlow.emit(stateFlow.value.copy(connected = true))
        }
    }

    fun onDisconnect() {
        if (!isAttached) return

        remoteParticipant.removeDelegate(delegate)
        isAttached = false

        scope.async {
            stateFlow.emit(stateFlow.value.copy(connected = false))
        }
    }

    /**
     * Called from the Room delegate when this remote participant's attributes
     * change. The Participant-scoped delegate also has a hook for this, but
     * Meet Web (and LiveKit Components) observes the Room event in practice,
     * which is more reliable for remotes.
     */
    fun onAttributesUpdatedFromRoom(attributes: Map<String, String>) {
        scope.async {
            stateFlow.emit(stateFlow.value.copy(attributes = attributes))
        }
    }

    /**
     * The wrapper for [track], registered on this participant. LiveKit delivers the
     * publish and subscribe callbacks for the same track back to back from its own
     * queues, so this find-or-create MUST be atomic: two wrappers for one sid meant the
     * published flag landed on one object and the subscribed flag on the other, and the
     * consumer - which keys tracks by sid - kept whichever came first and never saw the
     * other flag. Also re-reads the publication like the Android side does, so a
     * republished info (dimensions) is picked up.
     */
    private fun getOrCreate(track: RemoteTrackPublication): RemoteTrack {
        val sid = track.sid().stringValue()
        val (wrapper, isNew) = getOrAppend(
            matches = { it.sid == sid },
            create = {
                when (kindFrom(track.kind())) {
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
