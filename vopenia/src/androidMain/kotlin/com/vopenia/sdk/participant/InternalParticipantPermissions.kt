package com.vopenia.sdk.participant

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

    fun toMultiplatform() = ParticipantPermissions(
        isHidden = isHidden,
        isRecorder = isRecorder,
        canPublish = canPublish,
        canPublishData = canPublishData,
        canSubscribe = canSubscribe
    )
}
