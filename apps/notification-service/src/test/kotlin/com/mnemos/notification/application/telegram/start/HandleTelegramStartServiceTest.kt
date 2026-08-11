package com.mnemos.notification.application.telegram.start

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.recipient.model.RecipientView
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import com.mnemos.notification.domain.recipient.model.RecipientPriority
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class HandleTelegramStartServiceTest {
    private val renderer = mock<NotificationMessageRenderer>()
    private val gateway = mock<MessageDeliveryGateway>()
    private val recipientReader = mock<RecipientReader>()
    private val service = HandleTelegramStartService(renderer, gateway, recipientReader)

    @Test
    fun `start rendering receives resolved user id`() {
        val telegramId = TelegramId(123L)
        val userId = UserId(UUID.randomUUID())
        val context = TelegramStartContext(telegramId = telegramId.id, firstName = "Knalis")
        val rendered = RenderedMessage("start")
        whenever(recipientReader.findByTelegramId(telegramId)).thenReturn(
            RecipientView(
                userId = userId,
                telegramId = telegramId,
                priority = RecipientPriority.STANDARD,
            ),
        )
        whenever(renderer.renderStart(any())).thenReturn(rendered)
        whenever(gateway.deliver(telegramId, rendered)).thenReturn(DeliveryResult.Delivered)

        assertEquals(DeliveryResult.Delivered, service.handle(context))
        verify(renderer).renderStart(eq(context.copy(userId = userId)))
    }

    @Test
    fun `unknown recipient is rendered without user id`() {
        val telegramId = TelegramId(123L)
        val context = TelegramStartContext(telegramId = telegramId.id, username = "knalis")
        val rendered = RenderedMessage("start")
        whenever(recipientReader.findByTelegramId(telegramId)).thenReturn(null)
        whenever(renderer.renderStart(any())).thenReturn(rendered)
        whenever(gateway.deliver(telegramId, rendered)).thenReturn(DeliveryResult.Delivered)

        assertEquals(DeliveryResult.Delivered, service.handle(context))
        verify(renderer).renderStart(eq(context.copy(userId = null)))
    }
}
