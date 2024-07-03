package io.vopenia.app.content.join

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.codlab.compose.widgets.TextNormal
import eu.codlab.viewmodel.rememberViewModel
import io.vopenia.app.LocalApp
import io.vopenia.app.preview.PreviewWrapperLightColumn

@Composable
fun JoinScreen(
    modifier: Modifier = Modifier
) {
    val app = LocalApp.current
    val model = rememberViewModel("join_screen") { JoinScreenModel(app) }
    val state by model.states.collectAsState()

    Column(
        modifier = modifier.imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.widthIn(0.dp, 250.dp),
            contentAlignment = Alignment.Center
        ) {
            val internalModifier = Modifier.fillMaxWidth()
            Column {
                OutlinedTextField(
                    modifier = internalModifier,
                    label = {
                        TextNormal(
                            text = "Participant's Name"
                        )
                    },
                    value = state.participant,
                    onValueChange = { model.participant = it }
                )

                OutlinedTextField(
                    modifier = internalModifier,
                    label = {
                        TextNormal(
                            text = "Room's Name"
                        )
                    },
                    value = state.room,
                    onValueChange = { model.room = it }
                )

                Button(
                    modifier = internalModifier,
                    enabled = model.room.text.isNotEmpty()
                            && model.participant.text.isNotEmpty(),
                    onClick = { model.join() }
                ) {
                    TextNormal(
                        text = "Join"
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun JoinScreenPreview() {
    PreviewWrapperLightColumn { modifier, isDark ->
        JoinScreen(modifier.fillMaxSize())
    }
}