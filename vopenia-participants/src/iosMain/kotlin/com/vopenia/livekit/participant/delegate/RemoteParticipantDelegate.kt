package com.vopenia.livekit.participant.delegate

import LiveKitClient.ConnectionQuality
import LiveKitClient.Participant
import LiveKitClient.ParticipantDelegateProtocol
import LiveKitClient.ParticipantPermissions
import LiveKitClient.RemoteParticipant
import LiveKitClient.RemoteTrackPublication
import LiveKitClient.StreamState
import LiveKitClient.TrackPublication
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.darwin.NSObject
import com.vopenia.livekit.participant.track.StreamState as KotlinStreamState

@Suppress("LongParameterList")
@OptIn(ExperimentalForeignApi::class)
class RemoteParticipantDelegate(
    private val onTrackPublished: (RemoteTrackPublication) -> Unit,
    private val onTrackUnpublished: (RemoteTrackPublication) -> Unit,
    private val onTrackSubscribed: (RemoteTrackPublication) -> Unit,
    private val onTrackUnsubscribed: (RemoteTrackPublication) -> Unit,
    // add error management for unsusbcribe
    private val onTrackPublicationIsMuted: (TrackPublication, isMuted: Boolean) -> Unit,
    private val onConnectionQuality: (ConnectionQuality) -> Unit,
    private val onIsSpeaking: (Boolean) -> Unit,
    private val onMetadataUpdated: (String?) -> Unit,
    private val onNameUpdated: (String?) -> Unit,
    private val onPermissionsUpdated: (ParticipantPermissions) -> Unit,
    private val onTrackStreamStateChanged: (RemoteTrackPublication, KotlinStreamState) -> Unit
) : ParticipantDelegateProtocol, NSObject() {
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun remoteParticipant(
        participant: RemoteParticipant,
        didPublishTrack: RemoteTrackPublication
    ) {
        onTrackPublished(didPublishTrack)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun remoteParticipant(
        participant: RemoteParticipant,
        didUnpublishTrack: RemoteTrackPublication
    ) {
        onTrackUnpublished(didUnpublishTrack)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun participant(
        participant: RemoteParticipant,
        didSubscribeTrack: RemoteTrackPublication
    ) {
        onTrackSubscribed(didSubscribeTrack)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun participant(
        participant: RemoteParticipant,
        didUnsubscribeTrack: RemoteTrackPublication
    ) {
        onTrackUnsubscribed(didUnsubscribeTrack)
    }

    override fun participant(
        participant: Participant,
        trackPublication: TrackPublication,
        didUpdateIsMuted: Boolean
    ) {
        onTrackPublicationIsMuted(trackPublication, didUpdateIsMuted)
    }

    override fun participant(
        participant: RemoteParticipant,
        trackPublication: RemoteTrackPublication,
        didUpdateStreamState: StreamState
    ) {
        val streamState = KotlinStreamState.fromNSInteger(didUpdateStreamState)

        onTrackStreamStateChanged(trackPublication, streamState!!)
    }

    override fun participant(
        participant: Participant,
        didUpdateConnectionQuality: ConnectionQuality
    ) {
        onConnectionQuality(didUpdateConnectionQuality)
    }

    override fun participant(participant: Participant, didUpdateIsSpeaking: Boolean) {
        println("kotlin didUpdateIsSpeaking $didUpdateIsSpeaking")
        onIsSpeaking(didUpdateIsSpeaking)
    }

    override fun participant(participant: Participant, didUpdateMetadata: String?) {
        onMetadataUpdated(didUpdateMetadata)
    }

    override fun participant(participant: Participant, didUpdateName: String) {
        onNameUpdated(didUpdateName)
    }

    override fun participant(
        participant: Participant,
        didUpdatePermissions: ParticipantPermissions
    ) {
        onPermissionsUpdated(didUpdatePermissions)
    }
}
