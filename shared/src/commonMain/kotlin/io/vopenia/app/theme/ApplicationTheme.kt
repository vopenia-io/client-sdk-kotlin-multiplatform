package io.vopenia.app.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.codlab.compose.theme.AppMaterialTheme
import eu.codlab.compose.theme.Material
import eu.codlab.compose.theme.ThemeEnvironment

val typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

val shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

val darkThemeEnvironment = createEnvironmentDark(
    ThemeEnvironment(
        material = Material(
            colors = darkColorScheme(
                primary = Color(0xFFBB86FC),
                inversePrimary = Color(0xFF3700B3),
                secondary = Color(0xFF03DAC5)
            ),
            typography = typography,
            shapes = shapes
        )
    )
)

val lightThemeEnvironment = createEnvironmentLight(
    ThemeEnvironment(
        material = Material(
            colors = lightColorScheme(
                primary = Color(0xFF6200EE),
                inversePrimary = Color(0xFF3700B3),
                secondary = Color(0xFF03DAC5)
            ),
            typography = typography,
            shapes = shapes
        )
    )
)

@Suppress("MagicNumber")
@Composable
fun ApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = AppMaterialTheme(
    darkTheme = darkTheme,
    darkEnvironment = darkThemeEnvironment,
    lightEnvironment = lightThemeEnvironment
) {
    content()
}
