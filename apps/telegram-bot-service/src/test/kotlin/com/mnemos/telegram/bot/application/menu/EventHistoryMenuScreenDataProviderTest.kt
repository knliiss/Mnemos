package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeMetadataResult
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOption
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOptions
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeSelection
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.events.port.out.EventWipeMetadataGateway
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
    fun `search prompt does not call event or wipe gateway`() {
        val eventGateway = RecordingEventHistoryGateway()
        val wipeGateway = RecordingEventWipeMetadataGateway()
        val provider = EventHistoryMenuScreenDataProvider(eventGateway, wipeGateway)
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
        assertEquals(0, eventGateway.queries.size)
        assertEquals(0, wipeGateway.calls)
    }

    @Test
    fun `feed resolves previous wipe and forwards detailed filters search and cursor`() {
        val currentWipe =
            EventHistoryWipeOption(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72"),
                number = 12,
                name = "Current",
                active = true,
            )
        val previousWipe =
            EventHistoryWipeOption(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"),
                number = 11,
                name = "Previous",
                active = false,
            )
        val eventGateway = RecordingEventHistoryGateway()
        val wipeGateway =
            RecordingEventWipeMetadataGateway(
                EventHistoryWipeMetadataResult.Available(
                    EventHistoryWipeOptions(
                        current = currentWipe,
                        previous = previousWipe,
                    ),
                ),
            )
        val provider = EventHistoryMenuScreenDataProvider(eventGateway, wipeGateway)
        var context = EventHistoryMenuContext.filter(emptyMap(), EventHistoryFilter.ITEM_DROP)

        context = EventHistoryMenuContext.search(context, "Excalibur")
        context = EventHistoryMenuContext.itemRarity(context, EventHistoryItemRarity.MYTHICAL)
        context = EventHistoryMenuContext.itemType(context, EventHistoryItemType.SWORD)
        context = EventHistoryMenuContext.wipe(context, EventHistoryWipeSelection.PREVIOUS)

        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.EVENTS,
                screenContext = context,
            )

        val data = provider.load(session) as TelegramMenuScreenData.EventHistory
        val feed = data.data as EventHistoryMenuScreenData.Feed
        val query = eventGateway.queries.single()

        assertEquals(EventHistoryFilter.ITEM_DROP, feed.filter)
        assertEquals("Excalibur", feed.search)
        assertEquals(EventHistoryItemRarity.MYTHICAL, feed.itemRarity)
        assertEquals(EventHistoryItemType.SWORD, feed.itemType)
        assertEquals(EventHistoryWipeSelection.PREVIOUS, feed.wipeSelection)
        assertEquals(previousWipe, feed.wipeOptions.previous)
        assertEquals(EventHistoryFilter.ITEM_DROP, query.filter)
        assertEquals("Excalibur", query.search)
        assertEquals(EventHistoryItemRarity.MYTHICAL, query.itemRarity)
        assertEquals(EventHistoryItemType.SWORD, query.itemType)
        assertEquals(previousWipe.id, query.wipeId)
        assertEquals(1, eventGateway.queries.size)
        assertEquals(1, wipeGateway.calls)
        assertTrue(feed.result is EventHistoryResult.Available)
    }

    @Test
    fun `missing requested wipe falls back to all events`() {
        val currentWipe =
            EventHistoryWipeOption(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72"),
                number = 12,
                name = null,
                active = true,
            )
        val eventGateway = RecordingEventHistoryGateway()
        val wipeGateway =
            RecordingEventWipeMetadataGateway(
                EventHistoryWipeMetadataResult.Available(
                    EventHistoryWipeOptions(
                        current = currentWipe,
                        previous = null,
                    ),
                ),
            )
        val provider = EventHistoryMenuScreenDataProvider(eventGateway, wipeGateway)
        val context = EventHistoryMenuContext.wipe(emptyMap(), EventHistoryWipeSelection.PREVIOUS)
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.EVENTS,
                screenContext = context,
            )

        val data = provider.load(session) as TelegramMenuScreenData.EventHistory
        val feed = data.data as EventHistoryMenuScreenData.Feed
        val query = eventGateway.queries.single()

        assertEquals(EventHistoryWipeSelection.ALL, feed.wipeSelection)
        assertEquals(null, query.wipeId)
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

    private class RecordingEventWipeMetadataGateway(
        private val result: EventHistoryWipeMetadataResult = EventHistoryWipeMetadataResult.Unavailable,
    ) : EventWipeMetadataGateway {
        var calls: Int = 0
            private set

        override fun getOptions(): EventHistoryWipeMetadataResult {
            calls += 1
            return result
        }
    }
}
