package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.track.Source
import io.vopenia.livekit.participant.track.toSource
import io.livekit.android.room.participant.ParticipantPermission as PP

class InternalParticipantPermissions(
    val permissions: PP
) {
    val isHidden: Boolean
        get() = permissions.hidden

    val isRecorder: Boolean
        get() = permissions.recorder

    val canPublish: Boolean
        get() = permissions.canPublish

    val canPublishData: Boolean
        get() = permissions.canPublishData

    val canSubscribe: Boolean
        get() = permissions.canSubscribe

    // Empty list from LiveKit means "all sources allowed" — preserved as-is.
    val canPublishSources: Set<Source>
        get() = permissions.canPublishSources.map { it.toSource() }.toSet()

    fun toMultiplatform() = ParticipantPermissions(
        isHidden = isHidden,
        isRecorder = isRecorder,
        canPublish = canPublish,
        canPublishData = canPublishData,
        canSubscribe = canSubscribe,
        canPublishSources = canPublishSources
    )
}
