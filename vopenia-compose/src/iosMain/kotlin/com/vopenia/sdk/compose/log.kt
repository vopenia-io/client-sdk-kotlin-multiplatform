package com.vopenia.sdk.compose

actual fun log(tag: String, text: String) {
    println("$tag :: $text")
}