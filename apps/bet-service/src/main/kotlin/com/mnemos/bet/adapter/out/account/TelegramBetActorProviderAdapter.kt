package com.mnemos.bet.adapter.out.account

import com.mnemos.bet.application.security.port.out.TelegramBetActorProvider
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Component

@Component
class TelegramBetActorProviderAdapter(
    private val client: AccountInternalClient,
) : TelegramBetActorProvider {
    override fun findUserId(telegramId: TelegramId): UserId? =
        client
            .getTelegramUserIdentity(telegramId.id)
            ?.let { identity -> UserId(identity.userId) }
}
