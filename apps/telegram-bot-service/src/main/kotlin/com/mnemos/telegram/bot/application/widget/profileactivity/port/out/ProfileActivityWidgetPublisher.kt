package com.mnemos.telegram.bot.application.widget.profileactivity.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.profileactivity.ProfileActivityView

interface ProfileActivityWidgetPublisher {
    fun publish(telegramId: Long, view: ProfileActivityView): TelegramDeliveryResult

    fun update(telegramId: Long, messageId: Int, view: ProfileActivityView): TelegramDeliveryResult

    fun state(view: ProfileActivityView): String
}
