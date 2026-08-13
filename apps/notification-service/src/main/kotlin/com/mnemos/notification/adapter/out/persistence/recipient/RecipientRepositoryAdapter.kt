package com.mnemos.notification.adapter.out.persistence.recipient

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.domain.recipient.model.Recipient
import com.mnemos.notification.domain.recipient.model.RecipientDeliveryStatus
import com.mnemos.notification.domain.recipient.repository.RecipientRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class RecipientRepositoryAdapter(
    private val recipientRepository: RecipientJpaRepository,
) : RecipientRepository {
    override fun upsert(recipient: Recipient) {
        recipientRepository.upsert(
            userId = recipient.userId.id,
            telegramId = recipient.telegramId.id,
        )
    }

    @Transactional
    override fun activateByTelegramId(telegramId: TelegramId) {
        recipientRepository.updateDeliveryStatus(
            telegramId = telegramId.id,
            status = RecipientDeliveryStatus.ACTIVE,
        )
    }

    @Transactional
    override fun blockByTelegramId(telegramId: TelegramId) {
        recipientRepository.updateDeliveryStatus(
            telegramId = telegramId.id,
            status = RecipientDeliveryStatus.BLOCKED,
        )
    }
}
