package com.mnemos.telegram.bot.application.widget.accounts

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenAccountWidgetUseCase {
    val enabled: Boolean

    fun execute(
        telegramId: Long,
        page: Int = 0,
    ): TelegramDeliveryResult
}
