package com.mnemos.notification.application.recipient

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.domain.recipient.repository.RecipientRepository
import org.springframework.stereotype.Service

@Service
class ActivateTelegramRecipientService(
    private val repository: RecipientRepository,
) {
    fun execute(telegramId: Long) {
        repository.activateByTelegramId(TelegramId(telegramId))
    }
}
