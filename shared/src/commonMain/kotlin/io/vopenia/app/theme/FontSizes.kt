package io.vopenia.app.theme

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import eu.codlab.platform.Platform
import eu.codlab.platform.currentPlatform

data class FontSizes(
    val joinRoom: JoinRoomSize = JoinRoomSize(),
)

data class JoinRoomSize(
    val subTitle: TextUnit = 18.sp,
    val title: TextUnit = 20.sp,
    val copyright: TextUnit = 8.sp
)


private val defaultFontSizes = FontSizes()

private val jvmFontSizes = FontSizes(
    joinRoom = JoinRoomSize(
        subTitle = 14.sp,
        title = 18.sp,
        copyright = 6.sp
    ),
)

fun createFontSizes(platform: Platform = currentPlatform) = when (platform) {
    Platform.ANDROID -> defaultFontSizes
    Platform.IOS -> defaultFontSizes
    Platform.JVM -> jvmFontSizes
    Platform.JS -> jvmFontSizes
    Platform.LINUX -> jvmFontSizes
    Platform.MACOS -> jvmFontSizes
    Platform.WINDOWS -> jvmFontSizes
}
