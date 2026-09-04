package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.telegram.bot.application.identity.port.out.TelegramUserIdentityGateway
import org.springframework.stereotype.Component

@Component
class AccountTelegramUserIdentityGatewayAdapter(
    private val client: AccountServiceClient,
) : TelegramUserIdentityGateway {
    override fun bootstrap(
        telegramId: Long,
        username: String?,
    ): Boolean =
        try {
            client.bootstrapUserIdentity(
                telegramId = telegramId,
                username = username,
            )
            true
        } catch (_: RuntimeException) {
            false
        }
}
