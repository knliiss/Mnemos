package com.mnemos.telegram.bot.application.widget.notifications.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.notifications.NotificationWidgetView

interface NotificationWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: NotificationWidgetView,
    ): TelegramDeliveryResult

    fun update(
        telegramId: Long,
        messageId: Int,
        view: NotificationWidgetView,
    ): TelegramDeliveryResult

    fun state(view: NotificationWidgetView): String
}
