package com.mnemos.notification.domain.recipient.repository

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.domain.recipient.model.Recipient

interface RecipientRepository {
    fun upsert(recipient: Recipient)

    fun activateByTelegramId(telegramId: TelegramId)

    fun blockByTelegramId(telegramId: TelegramId)
}
