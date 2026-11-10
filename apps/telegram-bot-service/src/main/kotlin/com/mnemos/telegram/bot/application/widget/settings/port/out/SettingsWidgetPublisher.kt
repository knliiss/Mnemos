package com.mnemos.telegram.bot.application.widget.settings.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.settings.SettingsWidgetView

interface SettingsWidgetPublisher {
    fun publish(telegramId: Long, view: SettingsWidgetView): TelegramDeliveryResult
    fun update(telegramId: Long, messageId: Int, view: SettingsWidgetView): TelegramDeliveryResult
    fun state(view: SettingsWidgetView): String
}
