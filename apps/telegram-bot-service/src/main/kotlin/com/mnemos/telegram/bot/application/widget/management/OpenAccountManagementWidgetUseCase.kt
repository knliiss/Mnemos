package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenAccountManagementWidgetUseCase {
    val enabled: Boolean
    fun execute(telegramId: Long, page: Int = 0): TelegramDeliveryResult
}
