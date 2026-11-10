package com.mnemos.telegram.bot.application.widget.bets.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.bets.BetWidgetView

interface BetWidgetPublisher {
    fun publish(
        telegramId: Long,
        view: BetWidgetView,
    ): TelegramDeliveryResult

    fun update(
        telegramId: Long,
        messageId: Int,
        view: BetWidgetView,
    ): TelegramDeliveryResult

    fun state(view: BetWidgetView): String
}
