package com.mnemos.telegram.bot.application.widget.profiles.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetView

interface ProfileWidgetPublisher {
    fun publish(telegramId: Long, view: ProfileWidgetView): TelegramDeliveryResult

    fun update(telegramId: Long, messageId: Int, view: ProfileWidgetView): TelegramDeliveryResult

    fun state(view: ProfileWidgetView): String
}
