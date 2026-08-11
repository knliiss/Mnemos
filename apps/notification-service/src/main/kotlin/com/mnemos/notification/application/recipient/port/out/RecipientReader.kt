package com.mnemos.notification.application.recipient.port.out

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.recipient.model.RecipientView

interface RecipientReader {
    fun findByUserIds(userIds: Set<UserId>): Map<UserId, RecipientView>

    fun findByTelegramId(telegramId: TelegramId): RecipientView?
}
