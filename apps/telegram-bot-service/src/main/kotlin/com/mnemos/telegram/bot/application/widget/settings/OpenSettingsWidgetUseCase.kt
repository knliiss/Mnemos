package com.mnemos.telegram.bot.application.widget.settings

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenSettingsWidgetUseCase {
    val enabled: Boolean
    fun execute(telegramId: Long, page: Int = 0): TelegramDeliveryResult
}
