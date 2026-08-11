package com.mnemos.notification.adapter.out.persistence.recipient

import com.mnemos.notification.domain.recipient.model.Recipient
import com.mnemos.notification.domain.recipient.repository.RecipientRepository
import org.springframework.stereotype.Repository

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
}
