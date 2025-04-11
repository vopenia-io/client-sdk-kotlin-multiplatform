package io.vopenia.app.content.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import de.drick.compose.hotpreview.HotPreview
import eu.codlab.compose.theme.LocalDarkTheme
import io.vopenia.app.PreviewApp
import io.vopenia.app.theme.AppColor
import io.vopenia.app.utils.components.ButtonIcon
import io.vopenia.app.utils.components.CardButton

@Composable
fun BottomActions(
    modifier: Modifier,
    isMicActivated: Boolean,
    isVideoActivated: Boolean,
    onMicrophoneClick: () -> Unit,
    onVideoClick: () -> Unit,
    onLeave: () -> Unit
) {
    val frontColor = if (LocalDarkTheme.current) {
        AppColor.GrayExtraDark to AppColor.GrayLight
    } else {
        AppColor.GrayDark to AppColor.GrayLight
    }

    val microphoneInfo = if (isMicActivated) {
        Icons.Default.Mic to frontColor.first
    } else {
        Icons.Default.MicOff to frontColor.second
    }

    val videoInfo = if (isVideoActivated) {
        Icons.Default.Videocam to frontColor.first
    } else {
        Icons.Default.VideocamOff to frontColor.second
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        CardButton(
            leadingIcon = ButtonIcon(
                imageVector = microphoneInfo.first,
                colorFilter = ColorFilter.tint(Color.White)
            ),
            onClick = onMicrophoneClick,
            colors = CardDefaults.cardColors(microphoneInfo.second)
        )

        Spacer(modifier = Modifier.width(16.dp))

        CardButton(
            leadingIcon = ButtonIcon(
                imageVector = videoInfo.first,
                colorFilter = ColorFilter.tint(Color.White)
            ),
            onClick = onVideoClick,
            colors = CardDefaults.cardColors(videoInfo.second)
        )

        Spacer(modifier = Modifier.width(16.dp))

        CardButton(
            leadingIcon = ButtonIcon(
                imageVector = Icons.Default.CallEnd,
                colorFilter = ColorFilter.tint(Color.White)
            ),
            onClick = onLeave,
            colors = CardDefaults.cardColors(
                containerColor = AppColor.Red,
            )
        )
    }
}

@HotPreview(widthDp = 300, heightDp = 250, darkMode = true)
@HotPreview(widthDp = 300, heightDp = 250, darkMode = false)
@Composable
private fun BottomActionsPreview() {
    PreviewApp {
        Column(Modifier) {
            listOf(
                true to true,
                true to false,
                false to false,
                false to true
            ).forEach { (video, mic) ->
                BottomActions(
                    Modifier.fillMaxWidth(),
                    isVideoActivated = video,
                    isMicActivated = mic,
                    onMicrophoneClick = {
                        // nothing
                    },
                    onVideoClick = {
                        // nothing
                    },
                    onLeave = {
                        // nothing
                    }
                )
            }
        }
    }
}
