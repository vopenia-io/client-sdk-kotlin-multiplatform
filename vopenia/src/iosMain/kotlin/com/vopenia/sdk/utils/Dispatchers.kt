package com.vopenia.sdk.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual object Dispatchers {
    actual val IO = Dispatchers.IO
    actual val Main: CoroutineDispatcher = Dispatchers.Main
    actual val Default = Dispatchers.Default
    actual val Unconfined = Dispatchers.Unconfined
}
