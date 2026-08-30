package com.mnemos.personalization.application.ownership.usecase.getactivepack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.application.account.port.out.PersonalizationTelegramUserResolver
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackSelection
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service

@Service
class TelegramNotificationPackQueryService(
    private val telegramUserResolver: PersonalizationTelegramUserResolver,
    private val getActiveNotificationPackUseCase: GetActiveNotificationPackUseCase,
    private val personalizationRepository: PersonalizationRepository,
) {
    fun findByTelegramId(telegramId: TelegramId): TelegramNotificationPackSelection? {
        val userId = telegramUserResolver.findUserId(telegramId) ?: return null
        val selection = getActiveNotificationPackUseCase.execute(userId) ?: return null
        val personalization =
            personalizationRepository.findById(selection.personalizationId)
                ?: error("Active notification pack personalization was not found")

        return TelegramNotificationPackSelection(
            personalizationId = selection.personalizationId,
            name = personalization.name,
            selectionRevision = selection.selectionRevision,
            content = selection.content,
        )
    }
}
