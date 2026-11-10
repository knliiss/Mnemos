package com.mnemos.telegram.bot.application.widget.dashboard

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenDashboardWidgetUseCase {
    val enabled: Boolean

    fun execute(telegramId: Long): TelegramDeliveryResult
}
