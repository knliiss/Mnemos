package com.mnemos.personalization.adapter.out.account

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.account.port.out.PersonalizationTelegramUserResolver
import org.springframework.stereotype.Component

@Component
class PersonalizationTelegramUserResolverAdapter(
    private val client: PersonalizationAccountInternalClient,
) : PersonalizationTelegramUserResolver {
    override fun findUserId(telegramId: TelegramId): UserId? =
        client
            .getTelegramUserContext(telegramId.id)
            ?.let { response -> UserId(response.userId) }
}
