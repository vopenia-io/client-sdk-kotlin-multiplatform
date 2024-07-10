package io.vopenia.app.content.room

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import eu.codlab.compose.widgets.LocalWindow
import eu.codlab.compose.widgets.StatusBarAndNavigation
import eu.codlab.compose.widgets.screen.WindowType
import eu.codlab.safearea.views.SafeArea
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

    val localParticipant = room.localParticipant

    val columns = when (LocalWindow.current) {
        WindowType.SMARTPHONE_TINY -> 1
        WindowType.SMARTPHONE -> 1
        WindowType.TABLET -> 3
    }

    println("LocalWindow.current ${LocalWindow.current}")
    println("LocalWindow.current ${LocalWindow.current}")
    println("LocalWindow.current ${LocalWindow.current}")
    println("LocalWindow.current ${LocalWindow.current}")
    println("LocalWindow.current ${LocalWindow.current}")
    println("LocalWindow.current ${LocalWindow.current}")

    SafeArea {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns)
        ) {
            item(1) {
                LocalParticipantCell(Modifier, room, localParticipant)
            }

            items(participants.size) {
                RemoteParticipantCell(Modifier, room, participants[it])
            }
        }
    }
}