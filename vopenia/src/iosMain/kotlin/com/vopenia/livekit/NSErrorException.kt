package com.vopenia.livekit

import platform.Foundation.NSError

class NSErrorException(private val error: NSError) : Throwable() {
    override val message: String
        get() = "Error ${error.domain} ${error.localizedDescription}/${error.localizedFailureReason}"
}
