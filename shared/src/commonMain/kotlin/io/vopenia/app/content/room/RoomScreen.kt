package io.vopenia.app.content.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.codlab.compose.widgets.LocalWindow
import eu.codlab.compose.widgets.StatusBarAndNavigation
import eu.codlab.compose.widgets.screen.WindowType
import eu.codlab.safearea.views.SafeArea
import eu.codlab.viewmodel.rememberViewModel
import io.vopenia.app.LocalApp

@Composable
fun RoomScreen(
    modifier: Modifier = Modifier
) {
    StatusBarAndNavigation()

    val app = LocalApp.current
    val state by app.states.collectAsState()

    val room = state.room ?: return
    val participants by room.remoteParticipants.collectAsState()

    val model = rememberViewModel { RoomModel(room) }
    val participantCellsState by model.states.collectAsState()
    val cells = participantCellsState.participantCells

    val localParticipant = room.localParticipant
    println("on recomposition...")

    val columns = when (LocalWindow.current) {
        WindowType.SMARTPHONE_TINY -> 1
        WindowType.SMARTPHONE -> 1
        WindowType.TABLET -> 3
    }

    SafeArea {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(1) {
                LocalParticipantCell(Modifier, room, localParticipant)
            }

            items(cells.size) { index ->
                cells[index].let {
                    RemoteParticipantCell(Modifier, room, it.participant, it.videoTrack)
                }
            }
        }
    }
}