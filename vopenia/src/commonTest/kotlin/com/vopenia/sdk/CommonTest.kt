package com.vopenia.sdk

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CommonTest {
    @Test
    fun testExample() = runTest {
        // nothing for now

        val room = Room()
        room.connect("", "")
    }
}
