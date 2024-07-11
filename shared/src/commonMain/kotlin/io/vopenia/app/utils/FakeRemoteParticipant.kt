package io.vopenia.app.utils

import com.vopenia.sdk.participant.ParticipantPermissions
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipantState
import com.vopenia.sdk.utils.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRemoteParticipant : RemoteParticipant(
    scope = CoroutineScope(Dispatchers.Unconfined)
) {
    override val stateFlow = MutableStateFlow(
        RemoteParticipantState(
            connected = false,
            permissions = ParticipantPermissions()
        )
    )

    override val identity = "fake"
}