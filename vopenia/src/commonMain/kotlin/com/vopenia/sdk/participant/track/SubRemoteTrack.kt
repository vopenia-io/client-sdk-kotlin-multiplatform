package com.vopenia.sdk.participant.track

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class SubRemoteTrack(protected val scope: CoroutineScope) {
    private val remoteTrackState = MutableStateFlow(RemoteTrackState())
    val state = remoteTrackState.asStateFlow()

    private fun updateState(copy: RemoteTrackState.() -> RemoteTrackState) {
        scope.launch {
            remoteTrackState.emit(copy.invoke(remoteTrackState.value))
        }
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