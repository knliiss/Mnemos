package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.notification.NotificationCallbackData
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
    fun `notification menu renders subscriptions pagination and delete callbacks`() {
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
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.delete(subscriptionId) })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.page(0) })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.page(2) })
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.REFRESH })
    }

    @Test
    fun `unavailable notification menu keeps refresh navigation`() {
        val data =
            TelegramMenuScreenData.Notifications(
                NotificationMenuScreenData(
                    result = NotificationSubscriptionResult.Unavailable,
                ),
            )

        val message = NotificationMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Не удалось получить подписки"))
        assertTrue(callbacks.any { action -> action.data == NotificationCallbackData.REFRESH })
    }
}
