package com.mnemos.telegram.bot.application.input

import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface TelegramTextInputHandler {
    val pendingInput: TelegramPendingInput

    fun handle(context: TelegramTextInputContext): TelegramDeliveryResult
}

data class TelegramTextInputContext(
    val telegramId: Long,
    val text: String,
)
