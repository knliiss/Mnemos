package com.mnemos.telegram.bot.application.widget.wallet

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenWalletWidgetUseCase {
    val enabled: Boolean

    fun execute(
        telegramId: Long,
        page: Int = 0,
    ): TelegramDeliveryResult
}
