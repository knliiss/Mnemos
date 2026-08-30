package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
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
    fun `feed forwards active filter search and cursor`() {
        val gateway = RecordingEventHistoryGateway()
        val provider = EventHistoryMenuScreenDataProvider(gateway)
        val filteredContext = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.ITEM_DROP)
        val searchContext = EventHistoryMenuContext.search(filteredContext, "Excalibur")
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.EVENTS,
                screenContext = searchContext,
            )

        val data = provider.load(session) as TelegramMenuScreenData.EventHistory
        val feed = data.data as EventHistoryMenuScreenData.Feed

        assertEquals(EventHistoryFilter.ITEM_DROP, feed.filter)
        assertEquals("Excalibur", feed.search)
        assertEquals(1, gateway.queries.size)
        assertEquals(EventHistoryFilter.ITEM_DROP, gateway.queries.single().filter)
        assertEquals("Excalibur", gateway.queries.single().search)
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
