package com.vopenia.livekit.participant.track

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ITrack {
    val state: StateFlow<TrackState>
}

open class SubTrack(
    protected val scope: CoroutineScope,
    defaultState: TrackState
) : ITrack {
    var lastState = defaultState
        private set

    private val remoteTrackState = MutableStateFlow(lastState)
    override val state = remoteTrackState.asStateFlow()

    private fun updateState(copy: TrackState.() -> TrackState) {
        scope.launch {
            lastState = copy.invoke(lastState)
            remoteTrackState.emit(lastState)
        }
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
}
