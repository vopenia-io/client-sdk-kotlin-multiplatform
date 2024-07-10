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
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import eu.codlab.compose.widgets.TextNormal

@Composable
fun RemoteParticipantCell(
    modifier: Modifier,
    room: Room,
    participant: RemoteParticipant,
    videoTrack: RemoteVideoTrack? = null
) {
    val state by participant.state.collectAsState()

    Card {
        Column(modifier = modifier) {

            TextNormal(
                text = "Connected ${state}"
            )

            if (null != videoTrack) {
                val trackState by videoTrack.state.collectAsState()

                TextNormal(
                    text = "TrackState $trackState"
                )
            }

            videoTrack?.let {
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