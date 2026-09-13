package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.track.SubTrack
import io.vopenia.livekit.participant.track.TrackState
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The track registry and each track's flags are written from platform callbacks that are
 * NOT serialised with one another - on iOS LiveKit hands us didPublishTrack and
 * didSubscribeTrack back to back from its own queues, and the SDK scope is a thread pool.
 * Read-then-write lost one of the two updates there, which is how a screen share published
 * mid-call ended up with `published = false` and never became a tile at all.
 *
 * These tests pin the two contracts that prevent it: flag writes and registrations are
 * atomic, and they are visible as soon as the call returns (no scope to pump).
 */
class TrackRegistrationConcurrencyTest {

    private val hammer = 64

    // ----- per-track flags -------------------------------------------------

    @Test
    fun flagIsVisibleAsSoonAsItIsSet() = runTest {
        val track = SubTrack(backgroundScope, TrackState())

        track.setPublished(true)

        assertTrue(track.state.value.published, "published must not wait for a dispatch")
        assertTrue(track.lastState.published, "lastState must read through the flow")
    }

    @Test
    fun concurrentFlagWritesAllSurvive() = runTest {
        repeat(hammer) {
            val track = SubTrack(backgroundScope, TrackState())

            coroutineScope {
                launch(Dispatchers.Default) { track.setPublished(true) }
                launch(Dispatchers.Default) { track.setSubscribed(true) }
                launch(Dispatchers.Default) { track.setActive(true) }
                launch(Dispatchers.Default) { track.setMuted(true) }
                launch(Dispatchers.Default) { track.setDimensions(1280, 720) }
            }

            val state = track.state.value
            assertEquals(
                TrackState(
                    subscribed = true,
                    published = true,
                    active = true,
                    muted = true,
                    width = 1280,
                    height = 720,
                ),
                state,
                "a concurrent write clobbered another one",
            )
        }
    }

    // ----- the registry ----------------------------------------------------

    @Test
    fun registeringTheSameTrackTwiceKeepsOneWrapper() = runTest {
        val participant = FakeParticipant(backgroundScope)

        val (first, firstIsNew) = participant.register("sid-1")
        val (second, secondIsNew) = participant.register("sid-1")

        assertTrue(firstIsNew)
        assertTrue(!secondIsNew)
        assertSame(first, second)
        assertEquals(1, participant.tracks.value.size)
    }

    @Test
    fun concurrentRegistrationsOfDistinctTracksAllLand() = runTest {
        val participant = FakeParticipant(backgroundScope)

        coroutineScope {
            repeat(hammer) { index ->
                launch(Dispatchers.Default) { participant.register("sid-$index") }
            }
        }

        assertEquals(hammer, participant.tracks.value.size, "a registration was lost")
    }

    @Test
    fun concurrentRegistrationsOfOneTrackYieldOneWrapper() = runTest {
        val participant = FakeParticipant(backgroundScope)

        coroutineScope {
            repeat(hammer) {
                launch(Dispatchers.Default) { participant.register("sid-1") }
            }
        }

        assertEquals(1, participant.tracks.value.size, "the same track was registered twice")
    }

    /**
     * Publish and subscribe for ONE track, raced: whichever callback arrives first must
     * create the wrapper and the other must find it, so both flags end up on the same
     * object. Two wrappers for one sid is invisible to a consumer that keys tracks by
     * sid - it keeps the first and never sees the second one's flag.
     */
    @Test
    fun racedPublishAndSubscribeLandOnTheSameWrapper() = runTest {
        repeat(hammer) {
            val participant = FakeParticipant(backgroundScope)

            coroutineScope {
                launch(Dispatchers.Default) { participant.register("sid-1").first.setPublished(true) }
                launch(Dispatchers.Default) { participant.register("sid-1").first.setSubscribed(true) }
            }

            val tracks = participant.tracks.value
            assertEquals(1, tracks.size, "raced callbacks created two wrappers")
            val state = tracks.first().state.value
            assertTrue(state.published && state.subscribed, "one of the two flags was lost")
        }
    }
}

private class FakeTrack(scope: CoroutineScope, val key: String) : SubTrack(scope, TrackState())

private data class FakeState(
    override val metadata: String? = null,
    override val name: String? = null,
    override val permissions: ParticipantPermissions = ParticipantPermissions(),
    override val attributes: Map<String, String> = emptyMap(),
) : ParticipantState

private class FakeParticipant(
    scope: CoroutineScope,
) : Participant<FakeTrack, FakeState, FakeTrack, FakeTrack>(scope) {
    override val stateFlow = MutableStateFlow(FakeState())

    override val transcriptsFlow = MutableSharedFlow<TranscriptionSegment>()

    override val identity = "fake"

    override fun filterListAudio(tracks: List<FakeTrack>) = tracks

    override fun filterListVideo(tracks: List<FakeTrack>) = tracks

    fun register(key: String) = getOrAppend(
        matches = { it.key == key },
        create = { FakeTrack(scope, key) },
    )
}
