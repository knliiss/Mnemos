package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import org.springframework.stereotype.Component

@Component
class NotificationRecipientGatewayAdapter(
    private val client: NotificationServiceClient,
) : NotificationRecipientGateway {
    override fun activate(telegramId: Long) {
        client.activateRecipient(telegramId)
    }
}
