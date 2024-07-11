package com.vopenia.sdk.utils

import kotlinx.coroutines.CoroutineDispatcher

expect object Dispatchers {
    val IO: CoroutineDispatcher
    val Main: CoroutineDispatcher
    val Default: CoroutineDispatcher
    val Unconfined: CoroutineDispatcher
}
