package com.mnemos.telegram.bot.application.widget.dashboard.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView

interface DashboardWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: DashboardWidgetView,
    ): TelegramDeliveryResult
}
