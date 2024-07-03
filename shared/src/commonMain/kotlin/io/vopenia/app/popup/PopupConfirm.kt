package io.vopenia.app.popup


import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.codlab.compose.widgets.TextNormal
import eu.codlab.compose.widgets.TextTitle

@Suppress("LongMethod")
@Composable
fun PopupConfirm(
    show: Boolean,
    title: String,
    text: String,
    showCancel: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showed by remember { mutableStateOf(show) }
    var actualText by remember { mutableStateOf(text) }
    var actualTitle by remember { mutableStateOf(title) }

    LaunchedEffect(text) { actualText = text }
    LaunchedEffect(title) { actualTitle = title }

    LaunchedEffect(show) {
        if (show != showed) {
            showed = show
        }
    }

    if (showed) {
        AlertDialog(
            title = {
                TextTitle(
                    color = Color.Black,
                    text = actualTitle
                )
            },
            text = {
                TextNormal(
                    color = Color.Black,
                    text = actualText
                )
            },
            onDismissRequest = {
                showed = false
                onDismiss()
            },
            dismissButton = if (showCancel) {
                {
                    TextButton(
                        elevation = ButtonDefaults.elevation(0.dp),
                        onClick = {
                            showed = false
                            onDismiss()
                        }
                    ) {
                        TextNormal(
                            color = Color.Black,
                            text = "cancel"
                        )
                    }
                }
            } else {
                null
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showed = false
                        onConfirm()
                    }
                ) {
                    TextNormal(
                        color = Color.Black,
                        text = "confirm"
                    )
                }
            }
        )
    }
}
