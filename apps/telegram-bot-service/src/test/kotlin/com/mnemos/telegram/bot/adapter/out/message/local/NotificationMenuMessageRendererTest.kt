package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.notification.NotificationCallbackData
import com.mnemos.telegram.bot.application.notification.NotificationRaidLocations
import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropInputField
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuFeedback
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class NotificationMenuMessageRendererTest {
    @Test
    fun `notification dashboard uses compact category navigation`() {
        val activeId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page = emptyPage(totalItems = 17),
                            boosterSubscriptions = mapOf(NotificationBoosterType.LUCK to activeId),
                            dungeonSubscriptions = mapOf(NotificationDungeonType.HARD to activeId),
                            globalSubscriptions = mapOf(NotificationGlobalType.MOON to activeId),
                            raidSubscription = raidSubscription(setOf(3, 12)),
                        ),
                    feedback = NotificationMenuFeedback.RAID_SAVED,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val globals = callbacks.single { action -> action.data == NotificationCallbackData.GLOBALS }
        val boosters = callbacks.single { action -> action.data == NotificationCallbackData.BOOSTERS }
        val dungeons = callbacks.single { action -> action.data == NotificationCallbackData.DUNGEONS }
        val raid = callbacks.single { action -> action.data == NotificationCallbackData.RAID_EDIT }

        assertTrue(message.text.contains("Глобальные события: <b>1/5</b>"))
        assertTrue(message.text.contains("Бустеры: <b>1/4</b>"))
        assertTrue(message.text.contains("Dungeon: <b>1/4</b>"))
        assertTrue(message.text.contains("RAID: <b>2/28</b>"))
        assertTrue(message.text.contains("Дропы: <b>17</b>"))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, globals.style)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, boosters.style)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, dungeons.style)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, raid.style)
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.DROPS })
        assertFalse(
            callbacks.any { action ->
                action.data == NotificationCallbackData.global(NotificationGlobalType.MOON, enabled = false)
            },
        )
        assertEquals(listOf(2, 2, 1, 1, 1), message.actionRows.map(List<*>::size))
    }

    @Test
    fun `global category keeps direct event toggles in balanced grid`() {
        val activeId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page = emptyPage(),
                            globalSubscriptions = mapOf(NotificationGlobalType.MOON to activeId),
                        ),
                    mode = NotificationCreationMode.GLOBALS,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val moon =
            callbacks.single { action ->
                action.data == NotificationCallbackData.global(NotificationGlobalType.MOON, enabled = false)
            }
        val eclipse =
            callbacks.single { action ->
                action.data == NotificationCallbackData.global(NotificationGlobalType.ECLIPSE, enabled = true)
            }

        assertTrue(message.text.contains("<b>Глобальные события</b>"))
        assertTrue(message.text.contains("Включено: <b>1/5</b>"))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, moon.style)
        assertEquals(OutboundTelegramButtonStyle.DEFAULT, eclipse.style)
        assertEquals(listOf(3, 2, 1, 1), message.actionRows.map(List<*>::size))
    }

    @Test
    fun `booster and dungeon categories keep two by two toggle grids`() {
        val booster =
            NotificationMenuMessageRenderer.render(
                TelegramMenuScreenData.Notifications(
                    NotificationMenuScreenData(
                        result = NotificationSubscriptionResult.Available(page = emptyPage()),
                        mode = NotificationCreationMode.BOOSTERS,
                    ),
                ),
            )
        val dungeon =
            NotificationMenuMessageRenderer.render(
                TelegramMenuScreenData.Notifications(
                    NotificationMenuScreenData(
                        result = NotificationSubscriptionResult.Available(page = emptyPage()),
                        mode = NotificationCreationMode.DUNGEONS,
                    ),
                ),
            )

        assertEquals(listOf(2, 2, 1, 1), booster.actionRows.map(List<*>::size))
        assertEquals(listOf(2, 2, 1, 1), dungeon.actionRows.map(List<*>::size))
        assertTrue(
            booster.callbacks().any { action ->
                action.data == NotificationCallbackData.booster(NotificationBoosterType.LUCK, true)
            },
        )
        assertTrue(
            dungeon.callbacks().any { action ->
                action.data == NotificationCallbackData.dungeon(NotificationDungeonType.EASY, true)
            },
        )
    }

    @Test
    fun `drop list is a separate paginated screen`() {
        val subscriptionId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page =
                                NotificationSubscriptionPage(
                                    items =
                                        listOf(
                                            itemDropSubscription(
                                                id = subscriptionId,
                                                droppedFor = "knaliz&friend",
                                                itemName = "Sword <Art>",
                                            ),
                                        ),
                                    page = 1,
                                    totalPages = 3,
                                    totalItems = 17,
                                    hasNextPage = true,
                                ),
                        ),
                    mode = NotificationCreationMode.ITEM_DROP_LIST,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("📦 <b>Дропы</b>"))
        assertTrue(message.text.contains("Страница: <b>2/3</b>"))
        assertTrue(message.text.contains("Sword &lt;Art&gt;"))
        assertTrue(message.text.contains("knaliz&amp;friend"))
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.delete(subscriptionId) })
        assertTrue(callbacks.any { action -> action.label == "◀ 1/3" })
        assertTrue(callbacks.any { action -> action.label == "3/3 ▶" })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.REFRESH })
        assertEquals(listOf(1, 1, 2, 1, 1), message.actionRows.map(List<*>::size))
    }

    @Test
    fun `drop deletion shows selected rule and explicit confirmation`() {
        val subscriptionId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val subscription =
            itemDropSubscription(
                id = subscriptionId,
                droppedFor = "knaliz&friend",
                itemName = "Sword <Art>",
            )
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page =
                                NotificationSubscriptionPage(
                                    items = listOf(subscription),
                                    page = 1,
                                    totalPages = 3,
                                    totalItems = 17,
                                    hasNextPage = true,
                                ),
                        ),
                    mode = NotificationCreationMode.ITEM_DROP_DELETE,
                    deleteSubscriptionId = subscriptionId,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val confirmAction =
            callbacks.single { action ->
                action.data == NotificationCallbackData.confirmDelete(subscriptionId)
            }

        assertTrue(message.text.contains("🗑 <b>Удалить правило?</b>"))
        assertTrue(message.text.contains("Sword &lt;Art&gt;"))
        assertTrue(message.text.contains("knaliz&amp;friend"))
        assertTrue(message.text.contains("Это действие нельзя отменить"))
        assertEquals("🗑 Удалить", confirmAction.label)
        assertEquals(OutboundTelegramButtonStyle.DANGER, confirmAction.style)
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.DELETE_CANCEL })
        assertEquals(listOf(1, 1), message.actionRows.map(List<*>::size))
    }

    @Test
    fun `stale drop deletion cannot expose destructive confirmation`() {
        val subscriptionId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.Available(page = emptyPage()),
                    mode = NotificationCreationMode.ITEM_DROP_DELETE,
                    deleteSubscriptionId = subscriptionId,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Правило уже недоступно"))
        assertFalse(
            callbacks.any { action ->
                action.data == NotificationCallbackData.confirmDelete(subscriptionId)
            },
        )
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.DELETE_CANCEL })
    }

    @Test
    fun `item drop input prompt keeps cancel action and escapes current filters`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.NotLoaded,
                    mode = NotificationCreationMode.ITEM_DROP,
                    itemDropInput = NotificationItemDropInputField.ITEM,
                    droppedFor = "knaliz<1>",
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("knaliz&lt;1&gt;"))
        assertTrue(message.text.contains("1–24"))
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.ITEM_DROP_INPUT_CANCEL })
    }

    @Test
    fun `raid editor applies location targets immediately without save flow`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page = emptyPage(),
                            raidSubscription = raidSubscription(setOf(3, 12)),
                        ),
                    mode = NotificationCreationMode.RAID,
                    raidLocations = setOf(3, 12),
                    raidPage = 0,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val locationThree =
            callbacks.first { action ->
                action.data == NotificationCallbackData.raidLocation(location = 3, selected = false)
            }

        assertTrue(message.text.contains("RAID: <b>2/28</b>"))
        assertTrue(message.text.contains("3, 12"))
        assertEquals("3", locationThree.label)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, locationThree.style)
        assertTrue(
            callbacks.any { action ->
                action.data == NotificationCallbackData.raidLocation(location = 4, selected = true)
            },
        )
        assertTrue(callbacks.any { action -> action.label == "2/2 ▶" })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.RAID_DISABLE })
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.RAID_CLEAR })
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.RAID_SAVE })
        assertEquals(7, message.actionRows[0].size)
        assertEquals(7, message.actionRows[1].size)
    }

    @Test
    fun `missing raid is disabled but location targets can enable stable raid`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.Available(page = emptyPage()),
                    mode = NotificationCreationMode.RAID,
                    raidLocations = emptySet(),
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("RAID: <b>0/28</b>"))
        assertTrue(message.text.contains("└ Выкл"))
        assertTrue(
            callbacks.any { action ->
                action.data == NotificationCallbackData.raidLocation(location = 1, selected = true)
            },
        )
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.RAID_DISABLE })
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.RAID_SAVE })
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.CREATE })
        assertFalse(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
        assertEquals(listOf(7, 7, 1, 1), message.actionRows.map(List<*>::size))
    }

    @Test
    fun `all location raid renders all locations active`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page = emptyPage(),
                            raidSubscription = raidSubscription(emptySet()),
                        ),
                    mode = NotificationCreationMode.RAID,
                    raidLocations = NotificationRaidLocations.all,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val firstLocation =
            callbacks.first { action ->
                action.data == NotificationCallbackData.raidLocation(location = 1, selected = false)
            }

        assertTrue(message.text.contains("RAID: <b>28/28</b>"))
        assertTrue(message.text.contains("└ Все"))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, firstLocation.style)
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.RAID_DISABLE })
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.RAID_SAVE })
    }

    @Test
    fun `unavailable notification dashboard keeps refresh and back`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.Unavailable,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Не удалось получить настройки уведомлений"))
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.REFRESH })
    }

    private fun itemDropSubscription(
        id: UUID,
        droppedFor: String,
        itemName: String,
    ): NotificationSubscription =
        NotificationSubscription(
            id = id,
            type = NotificationSubscriptionType.ITEM_DROP,
            createdAt = Instant.parse("2026-08-30T12:24:00Z"),
            droppedFor = droppedFor,
            itemName = itemName,
            boosterType = null,
            dungeonType = null,
            globalType = null,
            raidLocations = emptySet(),
        )

    private fun raidSubscription(locations: Set<Int>): NotificationSubscription =
        NotificationSubscription(
            id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"),
            type = NotificationSubscriptionType.RAID,
            createdAt = Instant.parse("2026-08-30T12:24:00Z"),
            droppedFor = null,
            itemName = null,
            boosterType = null,
            dungeonType = null,
            globalType = null,
            raidLocations = locations,
        )

    private fun emptyPage(totalItems: Long = 0): NotificationSubscriptionPage =
        NotificationSubscriptionPage(
            items = emptyList(),
            page = 0,
            totalPages = 0,
            totalItems = totalItems,
            hasNextPage = false,
        )

    private fun OutboundTelegramMessage.callbacks(): List<OutboundTelegramMessageAction.Callback> =
        actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
}
