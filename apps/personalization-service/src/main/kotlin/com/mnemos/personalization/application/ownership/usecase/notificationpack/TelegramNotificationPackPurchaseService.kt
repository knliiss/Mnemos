package com.mnemos.personalization.application.ownership.usecase.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.application.account.port.out.PersonalizationTelegramUserResolver
import com.mnemos.personalization.application.purchase.usecase.purchase.PurchasePersonalizationResult
import com.mnemos.personalization.application.purchase.usecase.purchase.PurchasePersonalizationService
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TelegramNotificationPackPurchaseService(
    private val telegramUserResolver: PersonalizationTelegramUserResolver,
    private val personalizationRepository: PersonalizationRepository,
    private val purchaseService: PurchasePersonalizationService,
) {
    @Transactional
    fun purchase(
        telegramId: TelegramId,
        personalizationId: PersonalizationId,
        idempotencyKey: UUID,
    ): PurchasePersonalizationResult? {
        val userId = telegramUserResolver.findUserId(telegramId) ?: return null
        val personalization = personalizationRepository.findById(personalizationId) ?: return null

        if (personalization.type != PersonalizationType.NOTIFICATION_PACK) {
            return null
        }

        return purchaseService.executeForUser(
            userId = userId,
            personalizationId = personalizationId,
            idempotencyKey = idempotencyKey,
        )
    }
}
