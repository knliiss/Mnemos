package com.mnemos.telegram.bot.application.widget.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage

interface TelegramWidgetGateway {
    fun send(
        telegramId: Long,
        widget: TelegramWidgetMessage,
    ): TelegramDeliveryResult
}
