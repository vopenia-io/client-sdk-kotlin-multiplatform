package com.vopenia.sdk.utils

actual object Log {
    actual fun d(tag: String, text: String) {
        println("$tag :: $text")
    }
}
