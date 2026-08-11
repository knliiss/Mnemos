package com.mnemos.notification.domain.recipient.model

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId

class Recipient private constructor(
    val userId: UserId,
    val telegramId: TelegramId,
    val priority: RecipientPriority,
) {
    companion object {
        fun create(
            userId: UserId,
            telegramId: TelegramId,
        ): Recipient =
            Recipient(
                userId = userId,
                telegramId = telegramId,
                priority = RecipientPriority.STANDARD,
            )

        fun restore(
            userId: UserId,
            telegramId: TelegramId,
            priority: RecipientPriority,
        ): Recipient =
            Recipient(
                userId = userId,
                telegramId = telegramId,
                priority = priority,
            )
    }
}
