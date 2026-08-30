package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class EventHistoryMenuContextTest {
    @Test
    fun `next and previous preserve cursor history`() {
        val firstCursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val secondCursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")

        val firstPage = EventHistoryMenuContext.next(emptyMap(), firstCursor)
        val secondPage = EventHistoryMenuContext.next(firstPage, secondCursor)

        assertEquals(secondCursor, EventHistoryMenuContext.after(secondPage))
        assertTrue(EventHistoryMenuContext.hasPrevious(secondPage))

        val restoredFirstPage = EventHistoryMenuContext.previous(secondPage)
        val restoredRoot = EventHistoryMenuContext.previous(restoredFirstPage)

        assertEquals(firstCursor, EventHistoryMenuContext.after(restoredFirstPage))
        assertEquals(null, EventHistoryMenuContext.after(restoredRoot))
        assertFalse(EventHistoryMenuContext.hasPrevious(restoredRoot))
    }

    @Test
    fun `changing filter resets pagination state`() {
        val context = EventHistoryMenuContext.filter(EventHistoryFilter.GLOBAL)

        assertEquals(EventHistoryFilter.GLOBAL, EventHistoryMenuContext.selectedFilter(context))
        assertEquals(null, EventHistoryMenuContext.after(context))
        assertFalse(EventHistoryMenuContext.hasPrevious(context))
    }
}
