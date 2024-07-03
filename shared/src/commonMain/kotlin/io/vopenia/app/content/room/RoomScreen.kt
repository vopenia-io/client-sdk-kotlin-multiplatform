package io.vopenia.app.content.room

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import eu.codlab.compose.widgets.StatusBarAndNavigation
import eu.codlab.compose.widgets.TextNormal
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
    val roomState by room.connectionState.collectAsState()

    SafeArea {
        Column(modifier = modifier) {
            TextNormal(
                text = "$roomState"
            )
        }
    }
}