package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.track.Source

data class ParticipantPermissions(
    val isHidden: Boolean = true,
    val isRecorder: Boolean = false,
    val canPublish: Boolean = false,
    val canPublishData: Boolean = false,
    val canSubscribe: Boolean = false,
    /**
     * The track sources this participant is allowed to publish. **Empty = all
     * sources allowed** (LiveKit semantics — an empty allow-list is unrestricted).
     * Mirrors LiveKit's `ParticipantPermission.canPublishSources`; consumers should
     * gate publishing with `canPublish && (canPublishSources.isEmpty() || source in canPublishSources)`.
     */
    val canPublishSources: Set<Source> = emptySet(),
)
