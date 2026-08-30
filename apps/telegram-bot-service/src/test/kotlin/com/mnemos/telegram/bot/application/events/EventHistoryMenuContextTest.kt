package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryGlobalType
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
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
    fun `changing filter keeps search wipe and resets incompatible filters`() {
        val cursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val wipeId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        var context = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.ITEM_DROP)

        context = EventHistoryMenuContext.search(context, "knaliz")
        context = EventHistoryMenuContext.wipe(context, wipeId)
        context = EventHistoryMenuContext.itemRarity(context, EventHistoryItemRarity.SECRET)
        context = EventHistoryMenuContext.itemType(context, EventHistoryItemType.SWORD)
        context = EventHistoryMenuContext.next(context, cursor)
        context = EventHistoryMenuContext.filter(context, EventHistoryFilter.GLOBAL)

        assertEquals(EventHistoryFilter.GLOBAL, EventHistoryMenuContext.selectedFilter(context))
        assertEquals("knaliz", EventHistoryMenuContext.searchQuery(context))
        assertEquals(wipeId, EventHistoryMenuContext.selectedWipeId(context))
        assertEquals(null, EventHistoryMenuContext.selectedItemRarity(context))
        assertEquals(null, EventHistoryMenuContext.selectedItemType(context))
        assertEquals(null, EventHistoryMenuContext.after(context))
        assertFalse(EventHistoryMenuContext.hasPrevious(context))
    }

    @Test
    fun `selecting current filter preserves subtype filters`() {
        var context = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.ITEM_DROP)

        context = EventHistoryMenuContext.itemRarity(context, EventHistoryItemRarity.MYTHICAL)
        context = EventHistoryMenuContext.itemType(context, EventHistoryItemType.BOOK)
        context = EventHistoryMenuContext.filter(context, EventHistoryFilter.ITEM_DROP)

        assertEquals(EventHistoryItemRarity.MYTHICAL, EventHistoryMenuContext.selectedItemRarity(context))
        assertEquals(EventHistoryItemType.BOOK, EventHistoryMenuContext.selectedItemType(context))
    }

    @Test
    fun `new subtype filter resets event pagination`() {
        val cursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        var context = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.GLOBAL)

        context = EventHistoryMenuContext.next(context, cursor)
        context = EventHistoryMenuContext.globalType(context, EventHistoryGlobalType.ECLIPSE)

        assertEquals(EventHistoryGlobalType.ECLIPSE, EventHistoryMenuContext.selectedGlobalType(context))
        assertEquals(null, EventHistoryMenuContext.after(context))
        assertFalse(EventHistoryMenuContext.hasPrevious(context))
    }

    @Test
    fun `raid filter page does not reset event pagination`() {
        val cursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        var context = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.RAID)

        context = EventHistoryMenuContext.next(context, cursor)
        context = EventHistoryMenuContext.raidLocationPage(context, 2)

        assertEquals(2, EventHistoryMenuContext.selectedRaidLocationPage(context))
        assertEquals(cursor, EventHistoryMenuContext.after(context))
        assertTrue(EventHistoryMenuContext.hasPrevious(context))
    }

    @Test
    fun `new search keeps filter and resets pagination`() {
        val cursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val filteredContext = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.ITEM_DROP)
        val paginatedContext = EventHistoryMenuContext.next(filteredContext, cursor)

        val context = EventHistoryMenuContext.search(paginatedContext, "Excalibur")

        assertEquals(EventHistoryFilter.ITEM_DROP, EventHistoryMenuContext.selectedFilter(context))
        assertEquals("Excalibur", EventHistoryMenuContext.searchQuery(context))
        assertEquals(null, EventHistoryMenuContext.after(context))
        assertFalse(EventHistoryMenuContext.hasPrevious(context))
    }

    @Test
    fun `clearing search keeps selected filter`() {
        val filteredContext = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.RAID)
        val searchContext = EventHistoryMenuContext.search(filteredContext, "knaliz")

        val context = EventHistoryMenuContext.clearSearch(searchContext)

        assertEquals(EventHistoryFilter.RAID, EventHistoryMenuContext.selectedFilter(context))
        assertEquals(null, EventHistoryMenuContext.searchQuery(context))
    }
}
