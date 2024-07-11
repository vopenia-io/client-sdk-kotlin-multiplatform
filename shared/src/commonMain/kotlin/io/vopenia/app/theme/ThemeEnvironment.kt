package io.vopenia.app.theme

import androidx.compose.ui.graphics.Color
import eu.codlab.compose.theme.ColorBottomNavigations
import eu.codlab.compose.theme.ColorTheme
import eu.codlab.compose.theme.ThemeEnvironment

private val fullBlack = Color(0xff000000)
private val unselected = Color(0xff7a6b63)
private val selected = Color(0xffeec788)

private val colorNavigationsDark = ColorBottomNavigations(
    background = fullBlack,
    unselected = unselected,
    selected = selected
)

fun createEnvironmentDark(environment: ThemeEnvironment) = environment.copy(
    navigationColors = colorNavigationsDark,
    colors = ColorTheme(
        graySemiTransparent = AppColor.GraySemiTransparentLight
    ),
    gradientStart = AppColor.Primary,
    gradientEnd = AppColor.Blue
)

fun createEnvironmentLight(environment: ThemeEnvironment) = environment.copy(
    navigationColors = ColorBottomNavigations(
        background = fullBlack,
        unselected = unselected,
        selected = selected
    ),
    colors = ColorTheme(
        graySemiTransparent = AppColor.GraySemiTransparentDark
    ),
    gradientStart = Color.White,
    gradientEnd = AppColor.BlueLighter
)
