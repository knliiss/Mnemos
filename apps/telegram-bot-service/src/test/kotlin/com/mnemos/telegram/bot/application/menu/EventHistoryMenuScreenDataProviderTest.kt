package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class EventHistoryMenuScreenDataProviderTest {
    @Test
    fun `search prompt does not call event gateway`() {
        val gateway = RecordingEventHistoryGateway()
        val provider = EventHistoryMenuScreenDataProvider(gateway)
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.EVENTS,
                pendingInput = TelegramPendingInput.EVENT_SEARCH,
                screenContext =
                    EventHistoryMenuContext.withSearchError(
                        EventHistoryMenuContext.search(emptyMap(), "old query"),
                        "invalid query",
                    ),
            )

        val data = provider.load(session) as TelegramMenuScreenData.EventHistory
        val prompt = data.data as EventHistoryMenuScreenData.SearchPrompt

        assertEquals("old query", prompt.currentSearch)
        assertEquals("invalid query", prompt.error)
        assertEquals(0, gateway.queries.size)
    }

    @Test
    fun `feed forwards active detailed filters search and cursor`() {
        val gateway = RecordingEventHistoryGateway()
        val provider = EventHistoryMenuScreenDataProvider(gateway)
        val wipeId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        var context = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.ITEM_DROP)

        context = EventHistoryMenuContext.search(context, "Excalibur")
        context = EventHistoryMenuContext.itemRarity(context, EventHistoryItemRarity.MYTHICAL)
        context = EventHistoryMenuContext.itemType(context, EventHistoryItemType.SWORD)
        context = EventHistoryMenuContext.wipe(context, wipeId)

        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.EVENTS,
                screenContext = context,
            )

        val data = provider.load(session) as TelegramMenuScreenData.EventHistory
        val feed = data.data as EventHistoryMenuScreenData.Feed
        val query = gateway.queries.single()

        assertEquals(EventHistoryFilter.ITEM_DROP, feed.filter)
        assertEquals("Excalibur", feed.search)
        assertEquals(EventHistoryItemRarity.MYTHICAL, feed.itemRarity)
        assertEquals(EventHistoryItemType.SWORD, feed.itemType)
        assertEquals(wipeId, feed.wipeId)
        assertEquals(EventHistoryFilter.ITEM_DROP, query.filter)
        assertEquals("Excalibur", query.search)
        assertEquals(EventHistoryItemRarity.MYTHICAL, query.itemRarity)
        assertEquals(EventHistoryItemType.SWORD, query.itemType)
        assertEquals(wipeId, query.wipeId)
        assertEquals(1, gateway.queries.size)
        assertTrue(feed.result is EventHistoryResult.Available)
    }

    private class RecordingEventHistoryGateway : EventHistoryGateway {
        val queries = mutableListOf<EventHistoryQuery>()

        override fun getPage(query: EventHistoryQuery): EventHistoryResult {
            queries += query

            return EventHistoryResult.Available(
                EventHistoryPage(
                    items = emptyList(),
                    cursor = null,
                ),
            )
        }
    }
}
