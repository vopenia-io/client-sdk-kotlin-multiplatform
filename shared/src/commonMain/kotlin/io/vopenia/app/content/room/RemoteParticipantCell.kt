package io.vopenia.app.content.room

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vopenia.sdk.Room
import com.vopenia.sdk.compose.VideoView
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.track.Kind
import eu.codlab.compose.widgets.TextNormal

@Composable
fun RemoteParticipantCell(
    modifier: Modifier,
    room: Room,
    remoteParticipant: RemoteParticipant
) {
    val state by remoteParticipant.state.collectAsState()
    val tracks by remoteParticipant.tracks.collectAsState()

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

            tracks.forEach {
                TextNormal(
                    text = "Having track ${it.name} ${it.kind} ${it.isEnabled} ${it.state.value}"
                )

                if (it.kind == Kind.Video) {
                    VideoView(
                        modifier = Modifier.widthIn(300.dp)
                            .height(200.dp),
                        room = room,
                        track = it
                    )
                }
            }

        }
    }
}