package com.vopenia.livekit.participant.delegate

import LiveKitClient.ConnectionQuality
import LiveKitClient.LocalParticipant
import LiveKitClient.LocalTrackPublication
import LiveKitClient.Participant
import LiveKitClient.ParticipantDelegateProtocol
import LiveKitClient.ParticipantPermissions
import LiveKitClient.TrackPublication
import LiveKitClient.TranscriptionSegment
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.darwin.NSObject
import com.vopenia.livekit.participant.transcription.TranscriptionSegment as TS

@OptIn(ExperimentalForeignApi::class)
class LocalParticipantDelegate(
    private val onTrackPublished: (TrackPublication) -> Unit,
    private val onTrackUnpublished: (TrackPublication) -> Unit,
    private val onTrackPublicationIsMuted: (TrackPublication, isMuted: Boolean) -> Unit,
    private val onConnectionQuality: (ConnectionQuality) -> Unit,
    private val onIsSpeaking: (Boolean) -> Unit,
    private val onMetadataUpdated: (String?) -> Unit,
    private val onNameUpdated: (String?) -> Unit,
    private val onPermissionsUpdated: (ParticipantPermissions) -> Unit,
    private val onTranscriptionSegmentsReceived: (List<TS>) -> Unit
) : ParticipantDelegateProtocol, NSObject() {
    override fun participant(
        participant: Participant,
        trackPublication: TrackPublication,
        didUpdateIsMuted: Boolean
    ) {
        onTrackPublicationIsMuted(trackPublication, didUpdateIsMuted)
    }

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun localParticipant(
        participant: LocalParticipant,
        didPublishTrack: LocalTrackPublication
    ) {
        onTrackPublished(didPublishTrack)
    }

    /*@Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun participant(
        participant: participant,
        trackPublication: TrackPublication,
        didReceiveTranscriptionSegments: List<TranscriptionSegment>
    ) {
        onTranscriptionSegmentsReceived(
            didReceiveTranscriptionSegments.map {
                TS(
                    id = it.id,
                    transient = it.transient,
                    text = it.text
                )
            }
        )
    }*/

    @Suppress("CONFLICTING_OVERLOADS", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun localParticipant(
        participant: LocalParticipant,
        didUnpublishTrack: LocalTrackPublication
    ) {
        onTrackUnpublished(didUnpublishTrack)
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
