package io.vopenia.app.content.join

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vopenia.app.res.Res
import com.vopenia.app.res.join_background
import eu.codlab.compose.theme.LocalDarkTheme
import eu.codlab.compose.widgets.TextNormal
import eu.codlab.safearea.views.SafeArea
import eu.codlab.viewmodel.rememberViewModel
import io.vopenia.app.LocalApp
import io.vopenia.app.LocalFontSizes
import io.vopenia.app.preview.PreviewWrapperLightColumn
import org.jetbrains.compose.resources.painterResource

@Suppress("LongMethod")
@Composable
fun JoinScreen(
    modifier: Modifier = Modifier
) {
    val app = LocalApp.current
    val model = rememberViewModel("join_screen") { JoinScreenModel(app) }
    val state by model.states.collectAsState()

    Box(
        modifier = modifier.imePadding()
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        BackgroundDark()

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
                    TextNormal(
                        fontSize = LocalFontSizes.current.joinRoom.subTitle,
                        text = "Global"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextNormal(
                        fontSize = LocalFontSizes.current.joinRoom.subTitle,
                        text = "Real Time"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextNormal(
                        fontSize = LocalFontSizes.current.joinRoom.title,
                        fontWeight = FontWeight.Bold,
                        text = "Connectivity"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                        enabled = model.room.text.isNotEmpty() &&
                                model.participant.text.isNotEmpty(),
                        onClick = { model.join() }
                    ) {
                        TextNormal(
                            text = "Join"
                        )
                    }
                }
            }
        }

        Copyright()
    }
}

@Composable
private fun BackgroundDark() {
    if (!LocalDarkTheme.current) return
    Image(
        painter = painterResource(Res.drawable.join_background),
        contentDescription = "",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun Copyright() {
    SafeArea {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextNormal(
                fontSize = LocalFontSizes.current.joinRoom.copyright,
                text = "Copyright © 2024 - The Renegades"
            )
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
