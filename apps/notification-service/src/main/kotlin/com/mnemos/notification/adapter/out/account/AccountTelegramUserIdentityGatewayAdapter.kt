package com.mnemos.notification.adapter.out.account

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.recipient.port.out.TelegramUserIdentityGateway
import org.springframework.stereotype.Component

@Component
class AccountTelegramUserIdentityGatewayAdapter(
    private val client: NotificationAccountServiceClient,
) : TelegramUserIdentityGateway {
    override fun getOrCreate(telegramId: TelegramId): UserId? =
        try {
            UserId(
                client
                    .bootstrapTelegramUserIdentity(telegramId.id)
                    .userId,
            )
        } catch (_: RuntimeException) {
            null
        }
}
