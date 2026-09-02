package com.mnemos.telegram.bot.application.events

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EventHistorySearchQueryTest {
    @Test
    fun `search query trims text and keeps spaces`() {
        assertEquals("Sword Art", EventHistorySearchQuery.normalize("  Sword Art  "))
    }

    @Test
    fun `blank and oversized search queries are rejected`() {
        assertNull(EventHistorySearchQuery.normalize("   "))
        assertNull(EventHistorySearchQuery.normalize("a".repeat(65)))
    }
}
