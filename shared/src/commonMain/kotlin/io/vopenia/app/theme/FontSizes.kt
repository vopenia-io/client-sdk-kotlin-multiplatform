package io.vopenia.app.theme

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import eu.codlab.platform.Platform
import eu.codlab.platform.currentPlatform

data class FontSizes(
    val menu: MenuSize = MenuSize(),
    val userListItem: UserListItem = UserListItem(),
    val projectListItem: ProjectListItem = ProjectListItem(),
    val fleetListItem: FleetListItem = FleetListItem(),
    val actionBar: ActionBar = ActionBar()
)

data class MenuSize(
    val title: TextUnit = 14.sp,
    val item: TextUnit = 14.sp
)

data class UserListItem(
    val name: TextUnit = 14.sp,
    val email: TextUnit = 14.sp,
    val counterName: TextUnit = 14.sp,
    val counterValue: TextUnit = 12.sp,
)

data class ProjectListItem(
    val name: TextUnit = 14.sp,
    val info: TextUnit = 14.sp,
    val counterName: TextUnit = 14.sp,
    val counterValue: TextUnit = 12.sp,
    val subInfo: TextUnit = 12.sp,
)

data class FleetListItem(
    val name: TextUnit = 14.sp,
    val info: TextUnit = 14.sp,
    val counterName: TextUnit = 14.sp,
    val counterValue: TextUnit = 12.sp,
)

data class ActionBar(
    val title: TextUnit = TextUnit.Unspecified
)

private val defaultFontSizes = FontSizes()

private val jvmFontSizes = FontSizes(
    menu = MenuSize(),
    userListItem = UserListItem(
        name = 12.sp,
        email = 12.sp,
        counterName = 12.sp,
        counterValue = 10.sp,
    ),
    projectListItem = ProjectListItem(
        name = 12.sp,
        info = 12.sp,
        counterName = 12.sp,
        counterValue = 10.sp,
        subInfo = 10.sp
    ),
    fleetListItem = FleetListItem(
        name = 12.sp,
        info = 12.sp,
        counterName = 12.sp,
        counterValue = 10.sp,
    ),
    actionBar = ActionBar(
        title = 18.sp
    )
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
