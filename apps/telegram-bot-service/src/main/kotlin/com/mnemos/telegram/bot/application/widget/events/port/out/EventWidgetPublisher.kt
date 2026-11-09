package com.mnemos.telegram.bot.application.widget.events.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.events.EventWidgetView

interface EventWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: EventWidgetView,
    ): TelegramDeliveryResult

    fun update(
        telegramId: Long,
        messageId: Int,
        view: EventWidgetView,
    ): TelegramDeliveryResult

    fun state(view: EventWidgetView): String
}
