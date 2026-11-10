package com.mnemos.telegram.bot.application.widget.profilecomments.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.profilecomments.ProfileCommentsWidgetView

interface ProfileCommentsWidgetPublisher {
    fun publish(telegramId: Long, view: ProfileCommentsWidgetView): TelegramDeliveryResult
    fun update(telegramId: Long, messageId: Int, view: ProfileCommentsWidgetView): TelegramDeliveryResult
    fun state(view: ProfileCommentsWidgetView): String
}
