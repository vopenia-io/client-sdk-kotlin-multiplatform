package io.vopenia.app.content.pages.room

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.vopenia.livekit.Room
import com.vopenia.livekit.compose.ScaleType
import com.vopenia.livekit.compose.VideoView
import com.vopenia.livekit.participant.Participant
import com.vopenia.livekit.participant.track.IVideoTrack
import de.drick.compose.hotpreview.HotPreview
import eu.codlab.compose.theme.LocalDarkTheme
import eu.codlab.compose.widgets.TextNormal
import io.vopenia.app.LocalFontSizes
import io.vopenia.app.PreviewApp
import io.vopenia.app.theme.AppColor
import io.vopenia.app.utils.FakeRemoteParticipant

@Composable
fun ParticipantCell(
    modifier: Modifier,
    room: Room,
    participant: Participant<*, *, *, *>,
    videoTrack: IVideoTrack? = null
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
                RenderVideoTrack(
                    Modifier.fillMaxSize(),
                    avatarTint,
                    room,
                    videoTrack
                )
            } else {
                RenderAvatar(Modifier.fillMaxSize(), avatarTint)
            }

            RenderUserName(
                participant = participant
            )
        }
    }
}

@Composable
private fun RenderVideoTrack(
    modifier: Modifier,
    avatarTint: Color,
    room: Room,
    videoTrack: IVideoTrack
) {
    val trackState by videoTrack.state.collectAsState()

    if (trackState.published && !trackState.muted && trackState.active) {
        VideoView(
            modifier = modifier,
            room = room,
            track = videoTrack,
            scaleType = ScaleType.Fill
        )
    } else {
        RenderAvatar(modifier, avatarTint)
    }
}

@Composable
private fun RenderAvatar(modifier: Modifier, avatarTint: Color) {
    Image(
        modifier = modifier,
        imageVector = Icons.Default.Person,
        contentDescription = null,
        colorFilter = ColorFilter.tint(avatarTint)
    )
}

@Composable
fun RenderUserName(
    modifier: Modifier = Modifier,
    participant: Participant<*, *, *, *>
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

@HotPreview(widthDp = 100, heightDp = 150, darkMode = true)
@HotPreview(widthDp = 100, heightDp = 150, darkMode = false)
@Composable
private fun ParticipantCellPreview() {
    PreviewApp {
        ParticipantCell(
            modifier = Modifier.fillMaxSize(),
            room = Room(),
            participant = FakeRemoteParticipant(),
        )
    }
}
