package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface HandleWidgetTextReplyUseCase {
    fun handle(telegramId: Long, promptMessageId: Int, text: String): TelegramDeliveryResult?
}
