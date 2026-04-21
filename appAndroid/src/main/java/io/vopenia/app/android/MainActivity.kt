package io.vopenia.app.android

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import io.vopenia.livekit.PermissionsActivityController
import io.vopenia.app.App
import io.vopenia.app.AppBackPressProvider
import moe.tlaster.precompose.PreComposeApp

class MainActivity : FragmentActivity() {
    private val onBackPressProvider = AppBackPressProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionsActivityController.setActivity(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!onBackPressProvider.onBackPress()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PreComposeApp {
                Box {
                    App(
                        isDarkTheme = isSystemInDarkTheme(),
                        onBackPressed = onBackPressProvider,
                    )
                }
            }
        }
    }
}
