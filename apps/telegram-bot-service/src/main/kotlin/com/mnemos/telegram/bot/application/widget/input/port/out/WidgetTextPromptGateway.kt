package com.mnemos.telegram.bot.application.widget.input.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose

interface WidgetTextPromptGateway {
    fun prompt(telegramId: Long, widgetMessageId: Int, purpose: WidgetTextInputPurpose): TelegramMessageSendResult
}
