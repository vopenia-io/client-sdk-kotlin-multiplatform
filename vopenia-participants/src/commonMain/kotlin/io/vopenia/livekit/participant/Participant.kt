package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.chat.ChatMessage
import io.vopenia.livekit.participant.data.DataPacket
import io.vopenia.livekit.participant.track.SubTrack
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import io.vopenia.sdk.utils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class Participant<
        T : SubTrack,
        S : ParticipantState,
        A : T,
        V : T
        >(protected val scope: CoroutineScope) {
    protected val internalTracks = MutableStateFlow<List<T>>(emptyList())
    protected val isSpeakingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val tracks: StateFlow<List<T>> = internalTracks.asStateFlow()

    val videoTracks = internalTracks.map(scope) { filterListVideo(it) }
    val audioTracks = internalTracks.map(scope) { filterListAudio(it) }

    abstract fun filterListAudio(tracks: List<T>): List<A>

    abstract fun filterListVideo(tracks: List<T>): List<V>

    internal abstract val stateFlow: MutableStateFlow<S>

    internal abstract val transcriptsFlow: MutableSharedFlow<TranscriptionSegment>

    internal val dataReceivedFlowInternal = MutableSharedFlow<DataPacket>(extraBufferCapacity = 64)
    internal val chatMessagesFlowInternal = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64)

    abstract val identity: String?

    /**
     * Do [this] and [other] denote the same person in the room?
     *
     * [identity] is assigned by the LiveKit server, so it is `null` on a
     * handle built before the server answered. While either side is
     * unresolved, object identity is the only honest answer — treating two
     * not-yet-identified participants as equal merges distinct people into
     * one, silently dropping the second from any list deduplicated by
     * equality.
     */
    protected fun hasSameIdentityAs(other: Participant<*, *, *, *>): Boolean {
        if (this === other) return true

        val mine = identity ?: return false
        val theirs = other.identity ?: return false

        return mine == theirs
    }

    override fun equals(other: Any?): Boolean =
        other is Participant<*, *, *, *> && hasSameIdentityAs(other)

    val state: StateFlow<S>
        get() = stateFlow.asStateFlow()

    val transcripts: SharedFlow<TranscriptionSegment>
        get() = transcriptsFlow.asSharedFlow()

    val dataReceived: SharedFlow<DataPacket>
        get() = dataReceivedFlowInternal.asSharedFlow()

    val chatMessages: SharedFlow<ChatMessage>
        get() = chatMessagesFlowInternal.asSharedFlow()

    val isSpeakingState: StateFlow<Boolean>
        get() = isSpeakingFlow.asStateFlow()

    protected fun append(track: T) {
        scope.launch {
            internalTracks.emit(internalTracks.value + track)
        }
    }

    /**
     * Constant by design — do not "improve" this into a hash of [identity].
     *
     * [equals] keys on [identity], which the server assigns during connect:
     * `LocalParticipant.identity` reads `null` before it and non-null after.
     * A hash derived from a value that changes while the object sits in a
     * `HashMap` strands that entry in the wrong bucket and makes it
     * unreachable, so a constant is the only value that stays consistent
     * with [equals] across the whole lifetime of a handle. Participant sets
     * are call-sized, so the resulting linear probe within the single bucket
     * costs nothing measurable.
     *
     * This previously hashed `state.value`, which changes on every metadata,
     * permission and attribute update and has nothing to do with what
     * [equals] compares.
     */
    override fun hashCode(): Int = PARTICIPANT_HASH_CODE
}

/**
 * Shared by every [Participant] — see [Participant.hashCode].
 */
private const val PARTICIPANT_HASH_CODE = 0x50415254
