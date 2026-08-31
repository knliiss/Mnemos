package com.mnemos.notification.application.recipient

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import com.mnemos.notification.application.recipient.port.out.TelegramUserIdentityGateway
import org.springframework.stereotype.Service

@Service
class TelegramUserIdentityService(
    private val recipientReader: RecipientReader,
    private val identityGateway: TelegramUserIdentityGateway,
) {
    fun resolve(telegramId: TelegramId): UserId? =
        recipientReader.findByTelegramId(telegramId)?.userId
            ?: identityGateway.getOrCreate(telegramId)
}
