package com.mnemos.reward.application.daily.port.out

import com.mnemos.common.core.model.id.TelegramId

interface TelegramActiveAccountProvider {
    fun findByTelegramId(telegramId: TelegramId): ActiveAccountContext?
}
