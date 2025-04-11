package io.vopenia.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import eu.codlab.compose.widgets.CompositionScreenProvider
import eu.codlab.viewmodel.effects.LifecycleEffect
import eu.codlab.viewmodel.rememberViewModel
import io.vopenia.app.content.AppContent
import io.vopenia.app.popup.PopupConfirmCompose
import io.vopenia.app.popup.PopupLocalModel
import io.vopenia.app.theme.ApplicationTheme
import io.vopenia.app.theme.FontSizes
import io.vopenia.app.theme.createFontSizes
import io.vopenia.app.theme.modifier.defaultBackground
import io.vopenia.app.window.LocalFrameProvider

val staticModel: AppModelImpl = AppModelImpl()

val LocalApp = compositionLocalOf<AppModel> { error("No LocalApp model defined") }
val LocalConfirmPopup = compositionLocalOf<PopupLocalModel> { error("No LocalPopup") }
val LocalFontSizes = compositionLocalOf<FontSizes> { error("No LocalFontSizes") }

@Composable
fun App(
    isDarkTheme: Boolean,
    onBackPressed: AppBackPressProvider = AppBackPressProvider()
) {
    LifecycleEffect {
        println("Having a new lifecycle state $it")
    }

    val model = rememberViewModel { staticModel }

    InternalApp(
        modifier = Modifier.fillMaxSize(),
        model = model,
        isDarkTheme,
        onBackPressed
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .defaultBackground()
        ) {
            AppContent()

            PopupConfirmCompose()
        }
    }
}

@Composable
fun PreviewApp(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onBackPressed: AppBackPressProvider = AppBackPressProvider(),
    content: @Composable () -> Unit
) {
    // removed PreviewWindow & PrecomposeApp
    val model = AppModelPreview()

    InternalApp(
        modifier = modifier,
        model = model,
        isDarkTheme,
        onBackPressed
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .defaultBackground()
        ) {
            content()
        }
    }
}

@Composable
private fun InternalApp(
    modifier: Modifier = Modifier,
    model: AppModel,
    isDarkTheme: Boolean,
    onBackPressed: AppBackPressProvider = AppBackPressProvider(),
    content: @Composable () -> Unit
) {
    val confirmPopup by remember { mutableStateOf(PopupLocalModel()) }
    val fontSizes = createFontSizes()

    LaunchedEffect(onBackPressed) {
        model.onBackPressed = onBackPressed
    }

    CompositionLocalProvider(
        LocalConfirmPopup provides confirmPopup,
        LocalApp provides model,
        LocalFontSizes provides fontSizes,
    ) {
        ApplicationTheme(
            darkTheme = isDarkTheme
        ) {
            CompositionScreenProvider(modifier) {
                LocalFrameProvider {
                    content()
                }
            }
        }
    }
}
