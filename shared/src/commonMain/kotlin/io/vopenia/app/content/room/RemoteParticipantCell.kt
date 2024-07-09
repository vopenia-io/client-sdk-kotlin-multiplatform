package io.vopenia.app.content.room

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.vopenia.sdk.participant.remote.RemoteParticipant
import eu.codlab.compose.widgets.TextNormal

@Composable
fun RemoteParticipantCell(
    modifier: Modifier,
    remoteParticipant: RemoteParticipant
) {
    val state by remoteParticipant.state.collectAsState()

    Card {
        Column(modifier = modifier) {
            TextNormal(
                text = "Identity ${remoteParticipant.identity}"
            )

            TextNormal(
                text = "Name ${state.name}"
            )

            TextNormal(
                text = "Permissions ${state.permissions}"
            )

            TextNormal(
                text = "Connected ${state.connected}"
            )
        }
    }
}