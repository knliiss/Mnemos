package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenProfileWidgetUseCase {
    val enabled: Boolean

    fun execute(telegramId: Long): TelegramDeliveryResult
}
