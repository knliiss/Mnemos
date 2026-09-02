package com.mnemos.telegram.bot.application.notification.port.out

import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import java.util.UUID

interface NotificationSubscriptionGateway {
    fun list(
        telegramId: Long,
        page: Int,
        size: Int,
    ): NotificationSubscriptionResult

    fun delete(
        telegramId: Long,
        subscriptionId: UUID,
    ): Boolean
}
