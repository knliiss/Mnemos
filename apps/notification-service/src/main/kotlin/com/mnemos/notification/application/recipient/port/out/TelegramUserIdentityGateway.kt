package com.mnemos.notification.application.recipient.port.out

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId

fun interface TelegramUserIdentityGateway {
    fun getOrCreate(telegramId: TelegramId): UserId?
}
