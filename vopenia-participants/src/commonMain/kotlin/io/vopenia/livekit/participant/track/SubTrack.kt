package io.vopenia.livekit.participant.track

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface ITrack {
    val state: StateFlow<TrackState>
}

open class SubTrack(
    protected val scope: CoroutineScope,
    defaultState: TrackState
) : ITrack {
    private val remoteTrackState = MutableStateFlow(defaultState)
    override val state = remoteTrackState.asStateFlow()

    /** Latest state without collecting. Reads the flow, never a cached copy - see [updateState]. */
    val lastState: TrackState
        get() = remoteTrackState.value

    /**
     * Atomic read-modify-write, applied synchronously.
     *
     * The SDK scope is [kotlinx.coroutines.Dispatchers.IO], i.e. a thread POOL, and the
     * platform callbacks that drive these flags are not serialised with each other: on
     * iOS LiveKit delivers didPublishTrack and didSubscribeTrack back to back from its
     * own queues (Android funnels every event through a single collector, which is why
     * it never showed this). Mutating a cached `lastState` inside `scope.launch` let two
     * of those callbacks read the same snapshot and write it back, so the second silently
     * dropped the first flag - a track left with `published = false` is never turned into
     * a tile at all, which is how a screen share published mid-call went missing on iOS.
     *
     * Synchronous also matters: the state is complete BEFORE the wrapper is published to
     * [io.vopenia.livekit.participant.Participant.internalTracks], so a collector can
     * never observe it half-initialised.
     */
    private fun updateState(copy: TrackState.() -> TrackState) {
        remoteTrackState.update { copy.invoke(it) }
    }

    internal fun setMuted(muted: Boolean) {
        updateState { copy(muted = muted) }
    }

    internal fun setActive(active: Boolean) {
        updateState { copy(active = active) }
    }

    internal fun setSubscribed(subscribed: Boolean) {
        updateState { copy(subscribed = subscribed) }
    }

    internal fun setPublished(published: Boolean) {
        updateState { copy(published = published) }
    }

    internal fun setDimensions(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        updateState { copy(width = width, height = height) }
    }
}
