package com.mnemos.notification.application.recipient.model

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.recipient.model.RecipientPriority

data class RecipientView(
    val userId: UserId,
    val telegramId: TelegramId,
    val priority: RecipientPriority,
)
