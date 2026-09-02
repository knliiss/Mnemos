package com.mnemos.telegram.bot.application.online

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SaoOnlineCallbackDataTest {
    @Test
    fun `realm callback round trips positive realm number`() {
        val callbackData = SaoOnlineCallbackData.realm(14)

        assertEquals("on:r:14", callbackData)
        assertEquals(14, SaoOnlineCallbackData.realmNumber(callbackData))
    }

    @Test
    fun `invalid realm callback is rejected`() {
        assertNull(SaoOnlineCallbackData.realmNumber("on:r:0"))
        assertNull(SaoOnlineCallbackData.realmNumber("on:r:not-a-number"))
        assertNull(SaoOnlineCallbackData.realmNumber("m:online"))
    }
}
