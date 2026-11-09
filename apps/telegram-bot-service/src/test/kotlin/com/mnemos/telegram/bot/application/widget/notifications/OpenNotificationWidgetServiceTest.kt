package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import com.mnemos.telegram.bot.application.widget.notifications.port.out.NotificationWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenNotificationWidgetServiceTest {
    private val accounts = mock<AccountGateway>()
    private val subscriptions = mock<NotificationSubscriptionGateway>()
    private val publisher = mock<NotificationWidgetPublisher>()
    private val service =
        OpenNotificationWidgetService(TelegramWidgetProperties(enabled = true), accounts, subscriptions, publisher)

    @Test
    fun subscriptionDashboardReusesDomainGatewayAndPagination() {
        val accountId = UUID.randomUUID()
        whenever(accounts.getContext(11L)).thenReturn(
            TelegramAccountContextResult.Available(TelegramAccountContext(accountId, emptyList())),
        )
        whenever(subscriptions.list(11L, 0, 4)).thenReturn(
            NotificationSubscriptionResult.Available(
                page =
                    NotificationSubscriptionPage(
                        items = listOf(
                            NotificationSubscription(
                                id = UUID.randomUUID(),
                                type = NotificationSubscriptionType.ITEM_DROP,
                                createdAt = Instant.EPOCH,
                                droppedFor = "Kirito",
                                itemName = "Клинок",
                                boosterType = null,
                                dungeonType = null,
                                globalType = null,
                                raidLocations = emptySet(),
                            ),
                        ),
                        page = 0,
                        totalPages = 2,
                        totalItems = 5,
                        hasNextPage = true,
                    ),
                pingEnabled = true,
            ),
        )
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L, 0))
        verify(subscriptions).list(11L, 0, 4)
        verify(publisher).publish(eq(11L), any())
    }
}
