package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.adapter.out.notification.NotificationSubscriptionApiMapper.toModel
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NotificationSubscriptionGatewayAdapter(
    private val client: NotificationServiceClient,
) : NotificationSubscriptionGateway {
    override fun list(
        telegramId: Long,
        page: Int,
        size: Int,
    ): NotificationSubscriptionResult =
        try {
            NotificationSubscriptionResult.Available(
                client
                    .listSubscriptions(
                        telegramId = telegramId,
                        page = page,
                        size = size,
                    ).toModel(),
            )
        } catch (_: RuntimeException) {
            NotificationSubscriptionResult.Unavailable
        }

    override fun delete(
        telegramId: Long,
        subscriptionId: UUID,
    ): Boolean =
        try {
            client.deleteSubscription(
                telegramId = telegramId,
                subscriptionId = subscriptionId,
            )
            true
        } catch (_: RuntimeException) {
            false
        }
}
