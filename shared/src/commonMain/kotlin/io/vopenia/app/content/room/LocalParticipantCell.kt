package io.vopenia.app.content.room

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vopenia.sdk.Room
import com.vopenia.sdk.participant.local.LocalParticipant
import eu.codlab.compose.widgets.TextNormal

@Composable
fun LocalParticipantCell(
    modifier: Modifier,
    room: Room,
    localParticipant: LocalParticipant
) {
    val roomState by room.connectionState.collectAsState()
    val participantState by localParticipant.state.collectAsState()
    val isSpeaking by localParticipant.isSpeakingState.collectAsState()

    Card {
        Column(modifier = modifier) {
            TextNormal(
                color = Color.Red,
                text = "This is for testing purposes only regarding the real time comm features that the team will have access to."
            )
            TextNormal(
                text = "$roomState"
            )

            TextNormal(
                text = "isSpeaking $isSpeaking"
            )

            TextNormal(
                text = "name ${participantState.name}"
            )

            TextNormal(
                text = "identity ${localParticipant.identity}"
            )

            TextNormal(
                text = "metadata ${participantState.metadata}"
            )

            TextNormal(
                text = "permissions ${participantState.permissions}"
            )
        }
    }
}
