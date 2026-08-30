package com.mnemos.telegram.bot.application.online

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SaoOnlineSearchQueryTest {
    @Test
    fun `valid query is trimmed`() {
        assertEquals("knaliz", SaoOnlineSearchQuery.normalize("  knaliz  "))
    }

    @Test
    fun `invalid query is rejected`() {
        assertNull(SaoOnlineSearchQuery.normalize("x"))
        assertNull(SaoOnlineSearchQuery.normalize("two words"))
        assertNull(SaoOnlineSearchQuery.normalize("x".repeat(33)))
    }
}
