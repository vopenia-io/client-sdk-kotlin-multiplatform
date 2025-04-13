package io.vopenia.app.content.pages.room

import com.vopenia.sdk.participant.track.Source
import com.vopenia.sdk.participant.track.local.LocalTrack
import com.vopenia.sdk.utils.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrackAvailabilityChecker(
    private val scope: CoroutineScope,
    private val expectedSource: Source
) {
    private var knownTracks = mutableMapOf<String, LocalTrack>()
    private val mutableState = MutableStateFlow(false)
    val hasExpectedSource = mutableState.asStateFlow()

    fun checkAndStart(track: LocalTrack) {
        if (knownTracks.containsKey(track.sid)) return

        Log.d("RoomModel", "MicrophoneChecker:: adding the track ${track.source}")
        knownTracks[track.sid] = track
        start(track)
    }

    private fun start(track: LocalTrack) {
        if (track.source != expectedSource) return

        scope.launch {
            updateInternalState()

            track.state.collect {
                Log.d("RoomModel", "having a new state $it")
                updateInternalState()
            }
        }
    }

    private suspend fun updateInternalState() {
        // forcing no mic enabled
        var hasANonMutedTrack = false

        // as if one is... it will be enabled here
        knownTracks.values.forEach {
            if (it.source != expectedSource) return@forEach
            if (!it.lastState.muted && it.lastState.published) {
                hasANonMutedTrack = true
            }
        }

        Log.d("RoomModel", "now has $expectedSource ? $hasANonMutedTrack")
        mutableState.emit(hasANonMutedTrack)
    }
}
