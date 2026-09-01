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
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class EventHistoryMenuMessageRendererTest {
    @Test
    fun `event history renders compact filters typed items and cursor actions`() {
        val cursor = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.Feed(
                    filter = EventHistoryFilter.ALL,
                    search = "Sword <Art>",
                    sort = EventHistorySort.NEWEST,
                    itemRarity = null,
                    itemType = null,
                    boosterType = null,
                    globalType = null,
                    raidLocation = null,
                    raidLocationPage = 0,
                    wipeSelection = EventHistoryWipeSelection.ALL,
                    wipeOptions = EventHistoryWipeOptions(current = null, previous = null),
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

        assertTrue(message.text.contains("📜 <b>История событий</b>"))
        assertTrue(message.text.contains("└ Сортировка: <b>Новые</b>"))
        assertTrue(message.text.contains("├ Вайп: <b>Любой</b>"))
        assertTrue(message.text.contains("Поиск: <code>Sword &lt;Art&gt;</code>"))
        assertTrue(message.text.contains("Sword &lt;Art&gt;"))
        assertTrue(message.text.contains("knaliz&amp;friend"))
        assertTrue(message.text.contains("30.08 12:24 UTC"))
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.SEARCH_CLEAR })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.PREVIOUS })
        assertTrue(callbacks.any { action -> action.data == EventHistoryCallbackData.next(cursor) })
        assertTrue(
            callbacks.any { action ->
                action.data == EventHistoryCallbackData.filter(EventHistoryFilter.ALL) &&
                    action.style == OutboundTelegramButtonStyle.SUCCESS
            },
        )
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.HOME.callbackData })
        assertFalse(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
    }

    @Test
    fun `item drop history renders selected rarity type and wipe through button style`() {
        val currentWipe =
            EventHistoryWipeOption(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b74"),
                number = 12,
                name = "Season 12",
                active = true,
            )
        val previousWipe =
            EventHistoryWipeOption(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b75"),
                number = 11,
                name = null,
                active = false,
            )
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.Feed(
                    filter = EventHistoryFilter.ITEM_DROP,
                    search = null,
                    sort = EventHistorySort.NEWEST,
                    itemRarity = EventHistoryItemRarity.MYTHICAL,
                    itemType = EventHistoryItemType.SWORD,
                    boosterType = null,
                    globalType = null,
                    raidLocation = null,
                    raidLocationPage = 0,
                    wipeSelection = EventHistoryWipeSelection.CURRENT,
                    wipeOptions =
                        EventHistoryWipeOptions(
                            current = currentWipe,
                            previous = previousWipe,
                        ),
                    result = EventHistoryResult.Available(EventHistoryPage(emptyList(), null)),
                    hasPrevious = false,
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val mythical = callbacks.first { action -> action.data == EventHistoryCallbackData.itemRarity(EventHistoryItemRarity.MYTHICAL) }
        val sword = callbacks.first { action -> action.data == EventHistoryCallbackData.itemType(EventHistoryItemType.SWORD) }
        val current = callbacks.first { action -> action.data == EventHistoryCallbackData.wipe(EventHistoryWipeSelection.CURRENT) }

        assertTrue(message.text.contains("Редкость: <b>Mythical</b>"))
        assertTrue(message.text.contains("Предмет: <b>⚔️ Мечи</b>"))
        assertTrue(message.text.contains("Вайп: <b>Текущий #12 · Season 12</b>"))
        assertEquals("Mythical", mythical.label)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, mythical.style)
        assertEquals("⚔️ Мечи", sword.label)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, sword.style)
        assertEquals("Текущий #12", current.label)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, current.style)
    }

    @Test
    fun `raid history uses seven compact location buttons in one row`() {
        val data =
            TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.Feed(
                    filter = EventHistoryFilter.RAID,
                    search = null,
                    sort = EventHistorySort.NEWEST,
                    itemRarity = null,
                    itemType = null,
                    boosterType = null,
                    globalType = null,
                    raidLocation = 3,
                    raidLocationPage = 0,
                    wipeSelection = EventHistoryWipeSelection.ALL,
                    wipeOptions = EventHistoryWipeOptions(current = null, previous = null),
                    result = EventHistoryResult.Available(EventHistoryPage(emptyList(), null)),
                    hasPrevious = false,
                ),
            )

        val message = EventHistoryMenuMessageRenderer.render(data)
        val locationRow = message.actionRows.first { row -> row.size == 7 && row.all { action -> action.label.toIntOrNull() != null } }
        val locationThree = locationRow.filterIsInstance<OutboundTelegramMessageAction.Callback>().first { action -> action.label == "3" }

        assertEquals(7, locationRow.size)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, locationThree.style)
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
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Поиск по событиям"))
        assertTrue(message.text.contains("Фильтр: <b>📦 Дропы</b>"))
        assertTrue(message.text.contains("Текущий поиск: <code>old&lt;query&gt;</code>"))
        assertTrue(message.text.contains("invalid &lt;query&gt;"))
        assertEquals(1, callbacks.size)
        assertTrue(callbacks.single().data == EventHistoryCallbackData.SEARCH_CANCEL)
    }
}
