package com.mnemos.social.adapter.out.http.account

import com.mnemos.social.application.actor.model.TelegramSocialIdentity
import com.mnemos.social.application.actor.port.out.TelegramSocialIdentityPort
import org.springframework.stereotype.Component

@Component
class AccountTelegramSocialIdentityAdapter(
    private val accountClient: AccountServiceClient,
) : TelegramSocialIdentityPort {
    override fun resolve(telegramId: Long): TelegramSocialIdentity? =
        accountClient.getTelegramUserContext(telegramId)?.let {
            TelegramSocialIdentity(
                userId = it.userId,
                activeAccountId = it.activeAccountId,
            )
        }
}
