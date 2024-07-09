package com.vopenia.sdk.participant.delegate

import LiveKitClient.ConnectionQuality
import LiveKitClient.LocalParticipant
import LiveKitClient.LocalTrackPublication
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

@OptIn(ExperimentalForeignApi::class)
class RemoteParticipantDelegate(
    private val onTrackPublished: (RemoteTrackPublication) -> Unit,
    private val onTrackUnpublished: (RemoteTrackPublication) -> Unit,
    private val onTrackPublicationIsMuted: (TrackPublication, isMuted: Boolean) -> Unit,
    private val onConnectionQuality: (ConnectionQuality) -> Unit,
    private val onIsSpeaking: (Boolean) -> Unit,
    private val onMetadataUpdated: (String?) -> Unit,
    private val onNameUpdated: (String?) -> Unit,
    private val onPermissionsUpdated: (ParticipantPermissions) -> Unit


) : ParticipantDelegateProtocol, NSObject() {
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
        // onTrackPublished or unpublished?
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