package io.vopenia.app.content.room

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import io.vopenia.app.preview.PreviewWrapperDarkLightRow
import io.vopenia.app.theme.AppColor

@Composable
fun BottomActions(
    modifier: Modifier,
    onLeave: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier,
            onClick = onLeave,
            colors = CardDefaults.cardColors(
                containerColor = AppColor.Red,
            )
        ) {
            Image(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
                imageVector = Icons.Default.CallEnd,
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Preview
@Composable
private fun BottomActionsPreview() {
    PreviewWrapperDarkLightRow { modifier, _ ->
        BottomActions(
            modifier,
            onLeave = {

            }
        )
    }
}