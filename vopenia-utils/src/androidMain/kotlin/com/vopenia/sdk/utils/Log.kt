package com.vopenia.sdk.utils

import android.util.Log

actual object Log {
    actual fun d(tag: String, text: String) {
        Log.d(tag, text)
    }
}
