package com.mnemos.notification.adapter.out.persistence.recipient.query

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.recipient.model.RecipientView

object RecipientQueryMapper {
    fun RecipientViewProjection.toView(): RecipientView =
        RecipientView(
            userId = UserId(userId),
            telegramId = TelegramId(telegramId),
            priority = priority,
        )
}
