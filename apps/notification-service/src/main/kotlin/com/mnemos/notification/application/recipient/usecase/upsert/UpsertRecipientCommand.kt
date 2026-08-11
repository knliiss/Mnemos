package com.mnemos.notification.application.recipient.usecase.upsert

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId

data class UpsertRecipientCommand(
    val userId: UserId,
    val telegramId: TelegramId,
)
