package com.vopenia.sdk.participant

import com.vopenia.sdk.participant.track.SubTrack
import com.vopenia.sdk.utils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    abstract val identity: String?

    override fun equals(other: Any?): Boolean {
        if (other is Participant<*, *, *, *>) {
            return other.identity == identity
        }

        return false
    }

    val state: StateFlow<S>
        get() = stateFlow.asStateFlow()

    val isSpeakingState: StateFlow<Boolean>
        get() = isSpeakingFlow.asStateFlow()

    protected fun append(track: T) {
        scope.launch {
            internalTracks.emit(internalTracks.value + track)
        }
    }

    override fun hashCode(): Int {
        return state.value.hashCode()
    }
}
