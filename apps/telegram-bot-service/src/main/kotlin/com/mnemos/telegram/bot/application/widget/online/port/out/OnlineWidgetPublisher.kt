package com.mnemos.telegram.bot.application.widget.online.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetView

interface OnlineWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: OnlineWidgetView,
    ): TelegramDeliveryResult

    fun update(
        telegramId: Long,
        messageId: Int,
        view: OnlineWidgetView,
    ): TelegramDeliveryResult

    fun state(view: OnlineWidgetView): String
}
