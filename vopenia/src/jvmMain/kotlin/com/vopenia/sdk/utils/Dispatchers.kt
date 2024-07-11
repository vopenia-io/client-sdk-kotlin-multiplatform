package com.vopenia.sdk.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing

actual object Dispatchers {
    actual val IO = Dispatchers.IO
    actual val Main: CoroutineDispatcher = Dispatchers.Swing
    actual val Default: CoroutineDispatcher = Dispatchers.Swing
    actual val Unconfined = Dispatchers.Unconfined
}
