package com.mnemos.telegram.bot.application.widget.daily

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenDailyWidgetUseCase {
    val enabled: Boolean

    fun execute(telegramId: Long): TelegramDeliveryResult
}
