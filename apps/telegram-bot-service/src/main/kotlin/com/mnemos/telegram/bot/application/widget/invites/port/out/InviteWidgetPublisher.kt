package com.mnemos.telegram.bot.application.widget.invites.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.invites.InviteWidgetView

interface InviteWidgetPublisher {
    fun publish(telegramId: Long, view: InviteWidgetView): TelegramDeliveryResult

    fun update(telegramId: Long, messageId: Int, view: InviteWidgetView): TelegramDeliveryResult

    fun state(view: InviteWidgetView): String
}
