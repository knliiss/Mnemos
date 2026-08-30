package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.events.EventHistoryCallbackData
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class EventHistoryMenuMessageRendererTest {
    @Test
    fun `event history renders search typed items and cursor actions`() {
        val cursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.Feed(
                    filter = EventHistoryFilter.ALL,
                    search = "Sword <Art>",
                    result =
                        EventHistoryResult.Available(
                            EventHistoryPage(
                                items =
                                    listOf(
                                        EventHistoryItem.ItemDrop(
                                            eventId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
                                            occurredAt = Instant.parse("2026-08-30T12:24:00Z"),
                                            itemId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"),
                                            itemName = "Sword <Art>",
                                            itemRarity = "MYTHICAL",
                                            itemType = "SWORD",
                                            droppedFor = "knaliz&friend",
                                        ),
                                    ),
                                cursor = cursor,
                            ),
                        ),
                    hasPrevious = true,
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Поиск: <code>Sword &lt;Art&gt;</code>"))
        assertTrue(message.text.contains("Sword &lt;Art&gt;"))
        assertTrue(message.text.contains("knaliz&amp;friend"))
        assertTrue(message.text.contains("30.08 12:24 UTC"))
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH_CLEAR })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.PREVIOUS })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.next(cursor) })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.filter(EventHistoryFilter.ITEM_DROP) })
    }

    @Test
    fun `event history search prompt renders current search error and cancel`() {
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.SearchPrompt(
                    filter = EventHistoryFilter.ITEM_DROP,
                    currentSearch = "old<query>",
                    error = "invalid <query>",
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Поиск по событиям"))
        assertTrue(message.text.contains("Фильтр: <b>Дропы</b>"))
        assertTrue(message.text.contains("Текущий поиск: <code>old&lt;query&gt;</code>"))
        assertTrue(message.text.contains("invalid &lt;query&gt;"))
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH_CANCEL })
    }
}
