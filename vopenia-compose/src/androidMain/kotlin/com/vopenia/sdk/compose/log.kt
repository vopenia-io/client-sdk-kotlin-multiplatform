package com.vopenia.sdk.compose

import android.util.Log

actual fun log(tag: String, text: String) {
    Log.d(tag, text)
}