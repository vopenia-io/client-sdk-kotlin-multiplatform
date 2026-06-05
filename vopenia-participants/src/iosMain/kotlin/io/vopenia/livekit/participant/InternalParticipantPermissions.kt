package io.vopenia.livekit.participant

import LiveKitClient.ParticipantPermissions
import io.vopenia.livekit.participant.track.Source
import io.vopenia.livekit.participant.track.sourceFrom
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNumber

@OptIn(ExperimentalForeignApi::class)
class InternalParticipantPermissions(
    val permissions: ParticipantPermissions
) {
    val isHidden: Boolean
        get() = permissions.hidden()

    val isRecorder: Boolean
        get() = permissions.recorder()

    val canPublish: Boolean
        get() = permissions.canPublish()

    val canPublishData: Boolean
        get() = permissions.canPublishData()

    val canSubscribe: Boolean
        get() = permissions.canSubscribe()

    // Native `canPublishSources` is `Set<Track.Source.RawValue>` (integer raw values);
    // empty = all sources allowed. Bridging of the NSSet elements can surface them as
    // NSNumber or as boxed Int/Long depending on cinterop, so normalise to Long before
    // mapping through the existing `sourceFrom()` (unknown raw values are dropped).
    val canPublishSources: Set<Source>
        get() = permissions.canPublishSources().mapNotNull { element ->
            when (element) {
                is NSNumber -> element.longLongValue
                is Long -> element
                is Int -> element.toLong()
                else -> null
            }
        }.map { sourceFrom(it) }.toSet()

    fun toMultiplatform() = ParticipantPermissions(
        isHidden = isHidden,
        isRecorder = isRecorder,
        canPublish = canPublish,
        canPublishData = canPublishData,
        canSubscribe = canSubscribe,
        canPublishSources = canPublishSources
    )
}
