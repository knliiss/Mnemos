package com.mnemos.telegram.bot.application.widget.query.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface WidgetQueryResponder {
    fun reply(
        queryId: String,
        status: String,
        message: String,
        state: String? = null,
    ): TelegramDeliveryResult
}
