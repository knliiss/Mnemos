package com.mnemos.telegram.bot.application.widget.bets

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenBetWidgetUseCase {
    val enabled: Boolean

    fun execute(
        telegramId: Long,
        mode: BetMenuMode = BetMenuMode.ROUNDS,
    ): TelegramDeliveryResult
}
