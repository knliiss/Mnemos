package com.mnemos.bet.application.security.port.out

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId

fun interface TelegramBetActorProvider {
    fun findUserId(telegramId: TelegramId): UserId?
}
