package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenNotificationWidgetUseCase {
    val enabled: Boolean

    fun execute(
        telegramId: Long,
        page: Int = 0,
    ): TelegramDeliveryResult
}
