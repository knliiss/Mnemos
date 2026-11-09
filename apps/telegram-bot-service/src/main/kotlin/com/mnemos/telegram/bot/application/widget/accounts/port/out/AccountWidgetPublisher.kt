package com.mnemos.telegram.bot.application.widget.accounts.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetView

interface AccountWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: AccountWidgetView,
    ): TelegramDeliveryResult

    fun update(
        telegramId: Long,
        messageId: Int,
        view: AccountWidgetView,
    ): TelegramDeliveryResult

    fun state(view: AccountWidgetView): String
}
