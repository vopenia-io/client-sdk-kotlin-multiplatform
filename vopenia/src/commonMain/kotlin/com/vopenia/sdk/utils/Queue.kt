package com.vopenia.sdk.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Queue {
    private val scope = MainScope()
    private val internalQueue = Channel<Job>(Channel.UNLIMITED)

    init {
        scope.launch(Dispatchers.Default) {
            for (job in internalQueue) {
                try {
                    job.join()
                } catch (ignored: Throwable) {
                    // ignored
                }
            }
        }
    }

    fun post(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) = internalQueue.trySend(
        scope.launch(
            context,
            CoroutineStart.LAZY,
            block
        )
    )

    fun cancel() {
        internalQueue.cancel()
        scope.cancel()
    }
}
