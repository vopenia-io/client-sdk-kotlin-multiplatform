package io.vopenia.app.content.room

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.vopenia.sdk.Room
import com.vopenia.sdk.compose.ScaleType
import com.vopenia.sdk.compose.VideoView
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import eu.codlab.compose.theme.LocalDarkTheme
import eu.codlab.compose.widgets.TextNormal
import io.vopenia.app.LocalFontSizes
import io.vopenia.app.preview.PreviewWrapperLightColumn
import io.vopenia.app.theme.AppColor
import io.vopenia.app.utils.FakeRemoteParticipant

@Composable
fun RemoteParticipantCell(
    modifier: Modifier,
    room: Room,
    participant: RemoteParticipant,
    videoTrack: RemoteVideoTrack? = null
) {
    val avatarTint = if (LocalDarkTheme.current) {
        AppColor.Gray
    } else {
        AppColor.GrayDark
    }

    Card {
        Box(
            modifier = modifier
                .aspectRatio(1f),
            contentAlignment = Alignment.BottomStart
        ) {
            if (null != videoTrack) {
                VideoView(
                    modifier = Modifier.fillMaxSize(),
                    room = room,
                    track = videoTrack,
                    scaleType = ScaleType.Fill
                )
            } else {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(avatarTint)
                )
            }

            RenderUserName(
                participant = participant,
                videoTrack = videoTrack
            )
        }
    }
}

@Composable
fun RenderUserName(
    modifier: Modifier = Modifier,
    participant: RemoteParticipant,
    videoTrack: RemoteVideoTrack?
) {
    val state by participant.state.collectAsState()

    val nameBackground = if (LocalDarkTheme.current) {
        AppColor.Black
    } else {
        AppColor.GrayDark
    }

    val name = (state.name ?: participant.identity) ?: return

    Column(
        modifier = modifier.padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(nameBackground)
            .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
    ) {
        TextNormal(
            text = name,
            fontSize = LocalFontSizes.current.avatarSize.userName
        )
    }
}

@Preview
@Composable
private fun RemoteParticipantCellPreview() {
    PreviewWrapperLightColumn { modifier, _ ->
        RemoteParticipantCell(
            modifier = modifier.fillMaxSize(),
            room = Room(),
            participant = FakeRemoteParticipant(),
        )
    }
}