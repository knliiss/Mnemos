package com.mnemos.notification.application.recipient.usecase.upsert

import com.mnemos.notification.domain.recipient.model.Recipient
import com.mnemos.notification.domain.recipient.repository.RecipientRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpsertRecipientService(
    private val recipientRepository: RecipientRepository,
) : UpsertRecipientUseCase {
    override fun execute(command: UpsertRecipientCommand) {
        val recipient =
            Recipient.create(
                userId = command.userId,
                telegramId = command.telegramId,
            )
        recipientRepository.upsert(recipient)
    }
}
