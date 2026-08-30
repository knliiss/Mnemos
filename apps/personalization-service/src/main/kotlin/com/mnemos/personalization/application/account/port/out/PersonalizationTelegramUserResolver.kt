package com.mnemos.personalization.application.account.port.out

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId

fun interface PersonalizationTelegramUserResolver {
    fun findUserId(telegramId: TelegramId): UserId?
}
