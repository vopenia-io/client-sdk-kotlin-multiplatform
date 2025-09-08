package io.vopenia.app.content.pages.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vopenia.livekit.Room
import com.vopenia.sdk.utils.Log
import eu.codlab.compose.widgets.StatusBarAndNavigation
import eu.codlab.safearea.views.SafeArea
import eu.codlab.viewmodel.rememberViewModel
import io.vopenia.app.AppModel
import io.vopenia.app.LocalApp
import io.vopenia.app.theme.WindowType
import io.vopenia.app.window.LocalFrame

@Composable
fun RoomScreen(
    modifier: Modifier = Modifier
) {
    StatusBarAndNavigation()

    val app = LocalApp.current
    val state by app.states.collectAsState()

    val room = state.room ?: return

    val model = rememberViewModel { RoomModel(room) }
    val participantCellsState by model.states.collectAsState()
    val localCells = participantCellsState.localParticipantCells
    val remoteCells = participantCellsState.participantCells

    val columns = columns()

    SafeArea {
        Column {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth().weight(1f),
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                /*item(1) {
                    LocalParticipantCell(Modifier, room, localParticipant)
                }*/

                items(localCells.size) { index ->
                    localCells[index].let {
                        ParticipantCell(
                            Modifier.fillMaxWidth()
                                .aspectRatio(1f),
                            room,
                            room.localParticipant,
                            it.videoTrack
                        )
                    }
                }

                items(remoteCells.size) { index ->
                    remoteCells[index].let {
                        ParticipantCell(
                            Modifier.fillMaxWidth()
                                .aspectRatio(1f),
                            room,
                            it.participant,
                            it.videoTrack
                        )
                    }
                }
            }

            RoomScreenBottomActions(
                Modifier.fillMaxWidth(),
                app,
                room
            )
        }
    }
}

@Composable
fun RoomScreenBottomActions(
    modifier: Modifier = Modifier,
    app: AppModel,
    room: Room,
) {
    val model = rememberViewModel { RoomModel(room) }

    val microphoneIsUsed by model.microphoneEnabledState.collectAsState()
    val cameraIsUsed by model.cameraEnabledState.collectAsState()

    BottomActions(
        modifier = modifier,
        isMicActivated = microphoneIsUsed,
        isVideoActivated = cameraIsUsed,
        onVideoClick = {
            Log.d("RoomScreen", "onVideoClick -> ${!cameraIsUsed}")
            model.enableCamera(!cameraIsUsed)
        },
        onMicrophoneClick = {
            Log.d("RoomScreen", "onMicrophoneClick -> ${!microphoneIsUsed}")
            model.enableMicrophone(!microphoneIsUsed)
        },
        onLeave = {
            app.leaveRoom()
        }
    )
}

@Suppress("MagicNumber")
@Composable
private fun columns() = when (LocalFrame.current) {
    WindowType.SMARTPHONE_TINY -> 1
    WindowType.SMARTPHONE -> 1
    WindowType.PHABLET -> 3
    WindowType.TABLET -> 3
}
