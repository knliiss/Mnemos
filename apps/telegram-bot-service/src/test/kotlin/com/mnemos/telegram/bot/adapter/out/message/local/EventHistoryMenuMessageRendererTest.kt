package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.events.EventHistoryCallbackData
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.model.EventHistorySort
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOption
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOptions
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeSelection
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class EventHistoryMenuMessageRendererTest {
    @Test
    fun `event feed keeps filters out of the event list controls`() {
        val cursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.Feed(
                    filter = EventHistoryFilter.ITEM_DROP,
                    search = "Sword <Art>",
                    sort = EventHistorySort.NEWEST,
                    itemRarity = EventHistoryItemRarity.MYTHICAL,
                    itemType = EventHistoryItemType.SWORD,
                    boosterType = null,
                    globalType = null,
                    raidLocation = null,
                    wipeSelection = EventHistoryWipeSelection.CURRENT,
                    result =
                        EventHistoryResult.Available(
                            EventHistoryPage(
                                items = listOf(itemDrop()),
                                cursor = cursor,
                            ),
                        ),
                    hasPrevious = true,
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("📜 <b>История событий</b>"))
        assertTrue(message.text.contains("🎛 📦 Дропы · Mythical · ⚔️ Мечи · Текущий вайп · Новые"))
        assertTrue(message.text.contains("🔎 <code>Sword &lt;Art&gt;</code>"))
        assertTrue(message.text.contains("Sword &lt;Art&gt;"))
        assertTrue(message.text.contains("knaliz&amp;friend"))
        assertTrue(message.text.contains("30.08 12:24 UTC"))
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.FILTERS })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH_CLEAR })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.PREVIOUS })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.next(cursor) })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
        assertFalse(callbacks.any { action -> action.data == EventHistoryCallbackData.filter(EventHistoryFilter.ITEM_DROP) })
        assertFalse(callbacks.any { action -> action.data == EventHistoryCallbackData.sort(EventHistorySort.NEWEST) })
        assertFalse(callbacks.any { action -> action.data == EventHistoryCallbackData.wipe(EventHistoryWipeSelection.CURRENT) })
    }

    @Test
    fun `filter editor owns item wipe and sort controls`() {
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.Filters(
                    filter = EventHistoryFilter.ITEM_DROP,
                    sort = EventHistorySort.NEWEST,
                    itemRarity = EventHistoryItemRarity.MYTHICAL,
                    itemType = EventHistoryItemType.SWORD,
                    boosterType = null,
                    globalType = null,
                    raidLocation = null,
                    raidLocationPage = 0,
                    wipeSelection = EventHistoryWipeSelection.CURRENT,
                    wipeOptions = wipeOptions(),
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val mythical = callback(callbacks, EventHistoryCallbackData.itemRarity(EventHistoryItemRarity.MYTHICAL))
        val sword = callback(callbacks, EventHistoryCallbackData.itemType(EventHistoryItemType.SWORD))
        val currentWipe = callback(callbacks, EventHistoryCallbackData.wipe(EventHistoryWipeSelection.CURRENT))

        assertTrue(message.text.contains("🎛 <b>Фильтры истории</b>"))
        assertTrue(message.text.contains("Редкость: <b>Mythical</b>"))
        assertTrue(message.text.contains("Предмет: <b>⚔️ Мечи</b>"))
        assertTrue(message.text.contains("Вайп: <b>Текущий #12 · Season 12</b>"))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, mythical.style)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, sword.style)
        assertEquals("Текущий #12", currentWipe.label)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, currentWipe.style)
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.FILTERS_CLOSE })
        assertFalse(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH })
        assertFalse(callbacks.any { action -> action.data == EventHistoryCallbackData.REFRESH })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
    }

    @Test
    fun `raid location controls exist only inside filter editor`() {
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.Filters(
                    filter = EventHistoryFilter.RAID,
                    sort = EventHistorySort.NEWEST,
                    itemRarity = null,
                    itemType = null,
                    boosterType = null,
                    globalType = null,
                    raidLocation = 3,
                    raidLocationPage = 0,
                    wipeSelection = EventHistoryWipeSelection.ALL,
                    wipeOptions = EventHistoryWipeOptions(current = null, previous = null),
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val locationRow =
            message.actionRows.first { row ->
                row.size == 7 && row.all { action -> action.label.toIntOrNull() != null }
            }
        val locationThree =
            locationRow
                .filterIsInstance<OutboundTelegramMessageAction.Callback>()
                .first { action -> action.label == "3" }

        assertEquals(7, locationRow.size)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, locationThree.style)
        assertTrue(message.callbacks().any { action -> action.data == EventHistoryCallbackData.FILTERS_CLOSE })
    }

    @Test
    fun `event history search prompt keeps only cancel action`() {
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.SearchPrompt(
                    filter = EventHistoryFilter.ITEM_DROP,
                    currentSearch = "old<query>",
                    error = "invalid <query>",
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Поиск по событиям"))
        assertTrue(message.text.contains("Фильтр: <b>📦 Дропы</b>"))
        assertTrue(message.text.contains("Текущий поиск: <code>old&lt;query&gt;</code>"))
        assertTrue(message.text.contains("invalid &lt;query&gt;"))
        assertEquals(1, callbacks.size)
        assertEquals(EventHistoryCallbackData.SEARCH_CANCEL, callbacks.single().data)
    }

    private fun itemDrop(): EventHistoryItem.ItemDrop =
        EventHistoryItem.ItemDrop(
            eventId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
            occurredAt = Instant.parse("2026-08-30T12:24:00Z"),
            itemId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"),
            itemName = "Sword <Art>",
            itemRarity = "MYTHICAL",
            itemType = "SWORD",
            droppedFor = "knaliz&friend",
        )

    private fun wipeOptions(): EventHistoryWipeOptions =
        EventHistoryWipeOptions(
            current =
                EventHistoryWipeOption(
                    id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b74"),
                    number = 12,
                    name = "Season 12",
                    active = true,
                ),
            previous =
                EventHistoryWipeOption(
                    id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b75"),
                    number = 11,
                    name = null,
                    active = false,
                ),
        )

    private fun OutboundTelegramMessage.callbacks(): List<OutboundTelegramMessageAction.Callback> =
        actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

    private fun callback(
        callbacks: List<OutboundTelegramMessageAction.Callback>,
        data: String,
    ): OutboundTelegramMessageAction.Callback = callbacks.first { action -> action.data == data }
}
