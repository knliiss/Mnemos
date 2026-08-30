package com.mnemos.personalization.application.ownership.usecase.getactivepack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.application.account.port.out.PersonalizationTelegramUserResolver
import com.mnemos.personalization.application.ownership.model.ActiveNotificationPackSelection
import org.springframework.stereotype.Service

@Service
class TelegramNotificationPackQueryService(
    private val telegramUserResolver: PersonalizationTelegramUserResolver,
    private val getActiveNotificationPackUseCase: GetActiveNotificationPackUseCase,
) {
    fun findByTelegramId(telegramId: TelegramId): ActiveNotificationPackSelection? {
        val userId = telegramUserResolver.findUserId(telegramId) ?: return null

        return getActiveNotificationPackUseCase.execute(userId)
    }
}
