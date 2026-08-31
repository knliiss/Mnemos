package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.notification.NotificationCallbackData
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropInputField
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class NotificationMenuMessageRendererTest {
    @Test
    fun `notification menu renders only item drop rules with numbered pagination`() {
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
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Активных подписок: <b>17</b>"))
        assertTrue(message.text.contains("Страница: <b>2/3</b>"))
        assertTrue(message.text.contains("Sword &lt;Art&gt;"))
        assertTrue(message.text.contains("knaliz&amp;friend"))
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.delete(subscriptionId) })
        assertTrue(callbacks.any { action -> action.label == "← 1/3" })
        assertTrue(callbacks.any { action -> action.label == "3/3 →" })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.REFRESH })
    }

    @Test
    fun `global screen renders stateful toggles instead of list entries`() {
        val activeId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page = emptyPage(),
                            globalSubscriptions = mapOf(NotificationGlobalType.MOON to activeId),
                        ),
                    mode = NotificationCreationMode.GLOBAL,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val moon = callbacks.first { action -> action.data == NotificationCallbackData.global(NotificationGlobalType.MOON) }
        val eclipse = callbacks.first { action -> action.data == NotificationCallbackData.global(NotificationGlobalType.ECLIPSE) }

        assertTrue(message.text.contains("Подписка на глобальное событие"))
        assertTrue(moon.label.startsWith("✅"))
        assertTrue(moon.style == OutboundTelegramButtonStyle.SUCCESS)
        assertFalse(eclipse.label.startsWith("✅"))
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
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("knaliz&lt;1&gt;"))
        assertTrue(message.text.contains("1–24"))
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.ITEM_DROP_INPUT_CANCEL })
    }

    @Test
    fun `raid editor uses existing subscription locations and update action`() {
        val raidId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73")
        val raidSubscription =
            NotificationSubscription(
                id = raidId,
                type = NotificationSubscriptionType.RAID,
                createdAt = Instant.parse("2026-08-30T12:24:00Z"),
                droppedFor = null,
                itemName = null,
                boosterType = null,
                dungeonType = null,
                globalType = null,
                raidLocations = setOf(3, 12),
            )
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            page = emptyPage(),
                            raidSubscription = raidSubscription,
                        ),
                    mode = NotificationCreationMode.RAID,
                    raidLocations = setOf(3, 12),
                    raidPage = 0,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("3, 12"))
        assertTrue(callbacks.any { action -> action.label == "✅ 3" })
        assertTrue(callbacks.any { action -> action.label == "2/2 →" })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.RAID_CLEAR })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.RAID_SAVE })
    }

    @Test
    fun `missing raid state does not expose create action`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.Available(page = emptyPage()),
                    mode = NotificationCreationMode.RAID,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("RAID"))
        assertFalse(callbacks.any { action -> action.data == NotificationCallbackData.RAID_SAVE })
        assertTrue(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
    }

    @Test
    fun `unavailable notification menu keeps refresh and navigation without create`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.Unavailable,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Не удалось получить подписки"))
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

    private fun emptyPage(): NotificationSubscriptionPage =
        NotificationSubscriptionPage(
            items = emptyList(),
            page = 0,
            totalPages = 0,
            totalItems = 0,
            hasNextPage = false,
        )
}
