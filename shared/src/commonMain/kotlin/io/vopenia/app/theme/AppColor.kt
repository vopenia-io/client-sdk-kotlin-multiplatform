package io.vopenia.app.theme

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.codlab.compose.widgets.preview.PreviewDarkLightRow

object AppColor {
    // Brand book colors*/
    val Primary = Color(0xFF7D33FF)
    val PrimaryLighter = Color(0xFF8e50fa)
    val PrimaryLight = Color(0xFFB083FF)
    val PrimaryLightLighter = Color(0xFFba96fa)
    val Blue = Color(0xFF3E44FE)
    val BlueLight = Color(0xFF35C8FF)
    val BlueLighter = Color(0xFFA5E6FF)
    val Pink = Color(0xFFFF2E7E)
    val PinkLight = Color(0xFFFF7AAD)
    val Black = Color(0xFF13141C)
    val GrayExtraDark = Color(0xFF2E2E31)
    val GrayDark = Color(0xFF717276)
    val Gray = Color(0xFFA0A1A4)
    val GrayLight = Color(0xFFA0A1A4)
    val GrayExtraLight = Color(0xFFE7E7E8)
    val GraySemiTransparentLight = Color(0x66FFFFFF)
    val GraySemiTransparentDark = Color(0x66000000)
    val Black10 = Color(0x1913141C)
    val Black05 = Color(0x0D13141C)
    val Red = Color(0xFFD63838)
    val Orange = Color(0xffc54e00)
    val Green = Color(0xff00a216)
    val White = Color(0xFFFFFFFF)
    val WhiteCream = Color(0xFFF5F5F5)

    val BackgroundLightBlue = Color(0xff183158)
    val BackgroundBlue = Color(0xff0d1a2e)
    val Transparent = Color.Transparent
}

@Preview
@Composable
fun PreviewColors() {
    PreviewDarkLightRow { modifier, _ ->
        Column(modifier) {
            listOf(
                AppColor.Primary,
                AppColor.PrimaryLighter,
                AppColor.PrimaryLight,
                AppColor.PrimaryLightLighter,
                AppColor.Blue,
                AppColor.BlueLight,
                AppColor.Pink,
                AppColor.PinkLight,
                AppColor.Black,
                AppColor.GrayExtraDark,
                AppColor.GrayDark,
                AppColor.Gray,
                AppColor.GrayLight,
                AppColor.GrayExtraLight,
                AppColor.Black10,
                AppColor.Black05,
                AppColor.Red,
                AppColor.BackgroundBlue
            ).map {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(
                            color = it
                        )
                )
            }
        }
    }
}
