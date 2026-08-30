package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.notification.NotificationCallbackData
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropInputField
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class NotificationMenuMessageRendererTest {
    @Test
    fun `notification menu renders subscriptions pagination delete and create callbacks`() {
        val subscriptionId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result =
                        NotificationSubscriptionResult.Available(
                            NotificationSubscriptionPage(
                                items =
                                    listOf(
                                        NotificationSubscription(
                                            id = subscriptionId,
                                            type = NotificationSubscriptionType.ITEM_DROP,
                                            createdAt = Instant.parse("2026-08-30T12:24:00Z"),
                                            droppedFor = "knaliz&friend",
                                            itemName = "Sword <Art>",
                                            boosterType = null,
                                            dungeonType = null,
                                            globalType = null,
                                            raidLocations = emptySet(),
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
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.page(0) })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.page(2) })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.REFRESH })
    }

    @Test
    fun `type selection renders every native notification category`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.NotLoaded,
                    mode = NotificationCreationMode.TYPE_SELECTION,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Новая подписка"))
        NotificationSubscriptionType.entries.forEach { type ->
            assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.type(type) })
        }
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
        assertTrue(message.text.contains("Отправьте название предмета"))
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.ITEM_DROP_INPUT_CANCEL })
    }

    @Test
    fun `raid creation renders selected locations page navigation and submit`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.NotLoaded,
                    mode = NotificationCreationMode.RAID,
                    raidLocations = setOf(3, 12),
                    raidPage = 0,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("3, 12"))
        assertTrue(callbacks.any { action -> action.label == "✓ 3" })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.raidPage(1) })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.RAID_CLEAR })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.RAID_CREATE })
    }

    @Test
    fun `unavailable notification menu keeps create refresh and navigation`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.Unavailable,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Не удалось получить подписки"))
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.REFRESH })
    }
}
