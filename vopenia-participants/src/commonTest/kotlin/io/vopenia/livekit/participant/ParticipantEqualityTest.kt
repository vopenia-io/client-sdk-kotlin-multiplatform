package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.chat.ChatMessage
import io.vopenia.livekit.participant.devices.AudioInputDevice
import io.vopenia.livekit.participant.devices.AudioRoute
import io.vopenia.livekit.participant.devices.CameraDevice
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.local.LocalParticipantState
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipantState
import io.vopenia.livekit.participant.track.RemoteAudioTrack
import io.vopenia.livekit.participant.track.RemoteTrack
import io.vopenia.livekit.participant.track.RemoteVideoTrack
import io.vopenia.livekit.participant.track.local.LocalAudioTrack
import io.vopenia.livekit.participant.track.local.LocalTrack
import io.vopenia.livekit.participant.track.local.LocalVideoTrack
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import io.vopenia.livekit.participant.video.VideoResolutionPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * The equals/hashCode contract for [Participant].
 *
 * Two properties are load-bearing and easy to regress:
 *  - equality keys on [Participant.identity], but only once the server has
 *    assigned one — two still-unidentified handles are different people;
 *  - the hash never moves, because the identity it would otherwise be derived
 *    from is assigned late and participant state mutates constantly.
 */
class ParticipantEqualityTest {

    private fun remote(identity: String?) = TestRemoteParticipant(identity)

    private fun local(identity: String?) = TestLocalParticipant(identity)

    // ----- equality on identity -------------------------------------------

    @Test
    fun distinctHandlesForOnePersonAreEqual() {
        val a = remote("alice")
        val b = remote("alice")

        assertEquals(a, b)
        assertEquals(b, a)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun differentIdentitiesAreNotEqual() {
        assertNotEquals<RemoteParticipant>(remote("alice"), remote("bob"))
    }

    @Test
    fun aParticipantIsNotEqualToAnArbitraryObject() {
        assertFalse(remote("alice").equals("alice"))
    }

    // ----- the null-identity window ---------------------------------------

    @Test
    fun unidentifiedParticipantsAreNotEachOther() {
        val first = remote(null)
        val second = remote(null)

        // The regression this guards: comparing two nulls as equal collapses
        // two people who have merely not been named yet into one.
        assertNotEquals<RemoteParticipant>(first, second)
        assertNotEquals<RemoteParticipant>(second, first)
    }

    @Test
    fun anUnidentifiedParticipantIsStillItself() {
        val participant = remote(null)

        assertEquals(participant, participant)
        assertTrue(listOf(participant).contains(participant))
    }

    @Test
    fun anUnidentifiedParticipantIsNotEqualToAnIdentifiedOne() {
        assertNotEquals<RemoteParticipant>(remote(null), remote("alice"))
        assertNotEquals<RemoteParticipant>(remote("alice"), remote(null))
    }

    // ----- local and remote are different sides ---------------------------

    @Test
    fun localAndRemoteHandlesNeverDenoteTheSameParticipant() {
        val localAlice = local("alice")
        val remoteAlice = remote("alice")

        assertFalse(localAlice == remoteAlice)
        assertFalse(remoteAlice == localAlice)
    }

    // ----- hash stability --------------------------------------------------

    @Test
    fun hashSurvivesStateChanges() {
        val participant = remote("alice")
        val before = participant.hashCode()

        participant.stateFlow.value = RemoteParticipantState(
            connected = true,
            metadata = "now with metadata",
            name = "Alice",
            permissions = ParticipantPermissions(canPublish = true),
            attributes = mapOf("handRaisedAt" to "1700000000")
        )

        assertEquals(before, participant.hashCode())
    }

    @Test
    fun hashSurvivesIdentityBeingAssignedByTheServer() {
        val participant = remote(null)
        val beforeConnect = participant.hashCode()

        participant.assignedIdentity = "alice"

        assertEquals(beforeConnect, participant.hashCode())
    }

    // ----- what the contract actually buys the caller ----------------------

    @Test
    fun aParticipantStaysFindableInAHashSetAfterItsStateChanges() {
        val participant = remote("alice")
        val set = hashSetOf<RemoteParticipant>(participant)

        participant.stateFlow.value = RemoteParticipantState(
            connected = true,
            name = "Alice",
            permissions = ParticipantPermissions(canPublish = true)
        )

        // Previously the hash tracked state.value, so this entry moved bucket
        // and became unreachable the moment anything about the participant
        // changed.
        assertTrue(set.contains(participant))
        assertTrue(set.contains(remote("alice")))
    }

    @Test
    fun aMapKeyedByParticipantResolvesAcrossHandles() {
        val jobs = hashMapOf<RemoteParticipant, String>()
        jobs[remote("alice")] = "alice-job"

        assertEquals("alice-job", jobs[remote("alice")])
        assertEquals(1, jobs.size)

        jobs[remote("alice")] = "replacement"
        assertEquals(1, jobs.size)
        assertEquals("replacement", jobs[remote("alice")])
    }

    @Test
    fun unidentifiedParticipantsKeepSeparateEntries() {
        val first = remote(null)
        val second = remote(null)
        val set = hashSetOf(first, second)

        assertEquals(2, set.size)
    }

    @Test
    fun distinctSetKeepsOneEntryPerPerson() {
        val participants = listOf(remote("alice"), remote("alice"), remote("bob"))

        assertEquals(2, participants.distinct().size)
    }
}

private val testScope = CoroutineScope(Dispatchers.Unconfined)

/**
 * A remote handle whose identity can be reassigned, mirroring the way the
 * LiveKit server fills it in during connect.
 */
private class TestRemoteParticipant(
    var assignedIdentity: String?
) : RemoteParticipant(
    scope = testScope,
    defaultState = RemoteParticipantState(
        connected = false,
        permissions = ParticipantPermissions()
    )
) {
    override val identity: String?
        get() = assignedIdentity

    override fun filterListAudio(tracks: List<RemoteTrack>) =
        tracks.filterIsInstance<RemoteAudioTrack>()

    override fun filterListVideo(tracks: List<RemoteTrack>) =
        tracks.filterIsInstance<RemoteVideoTrack>()
}

private class TestLocalParticipant(
    private val assignedIdentity: String?
) : LocalParticipant(testScope) {
    override val stateFlow = MutableStateFlow(
        LocalParticipantState(permissions = ParticipantPermissions())
    )

    override val transcriptsFlow = MutableSharedFlow<TranscriptionSegment>()

    override val identity: String?
        get() = assignedIdentity

    override fun filterListAudio(tracks: List<LocalTrack>) =
        tracks.filterIsInstance<LocalAudioTrack>()

    override fun filterListVideo(tracks: List<LocalTrack>) =
        tracks.filterIsInstance<LocalVideoTrack>()

    override suspend fun setAudioRoute(route: AudioRoute) = Unit

    override suspend fun enableMicrophone(enabled: Boolean, device: AudioInputDevice?) = Unit

    override suspend fun enableCamera(enabled: Boolean, device: CameraDevice?) = Unit

    override suspend fun switchCamera() = Unit

    override suspend fun publishData(payload: ByteArray, reliable: Boolean, topic: String?) = Unit

    override suspend fun updateAttributes(attributes: Map<String, String>) = Unit

    override suspend fun sendChatMessage(text: String) = ChatMessage(
        id = "test",
        timestamp = 0L,
        message = text,
        senderIdentity = assignedIdentity
    )

    override suspend fun startScreenShare() = Unit

    override suspend fun stopScreenShare() = Unit

    override suspend fun setVideoEffect(effect: VideoEffect?) = Unit

    override suspend fun setMaxSendingResolution(preset: VideoResolutionPreset) = Unit
}
