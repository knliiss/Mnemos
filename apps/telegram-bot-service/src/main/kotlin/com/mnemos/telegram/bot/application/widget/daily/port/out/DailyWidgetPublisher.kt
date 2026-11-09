package com.mnemos.telegram.bot.application.widget.daily.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.daily.DailyWidgetView

interface DailyWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: DailyWidgetView,
    ): TelegramDeliveryResult

    fun update(
        telegramId: Long,
        messageId: Int,
        view: DailyWidgetView,
    ): TelegramDeliveryResult

    fun state(view: DailyWidgetView): String
}
