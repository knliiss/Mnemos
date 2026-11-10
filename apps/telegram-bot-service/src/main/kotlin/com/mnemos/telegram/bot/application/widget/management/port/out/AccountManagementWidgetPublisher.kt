package com.mnemos.telegram.bot.application.widget.management.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.AccountManagementWidgetView

interface AccountManagementWidgetPublisher {
    fun publish(telegramId: Long, view: AccountManagementWidgetView): TelegramDeliveryResult
    fun update(telegramId: Long, messageId: Int, view: AccountManagementWidgetView): TelegramDeliveryResult
    fun state(view: AccountManagementWidgetView): String
}
