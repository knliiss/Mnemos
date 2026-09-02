package com.mnemos.telegram.bot.application.callback

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface TelegramScopedCallbackHandler {
    val callbackPrefix: String

    fun handle(context: TelegramCallbackContext): TelegramDeliveryResult
}
