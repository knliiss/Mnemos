package com.mnemos.telegram.bot.application.widget.online

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenOnlineWidgetUseCase {
    val enabled: Boolean

    fun execute(
        telegramId: Long,
        realmNumber: Int? = null,
    ): TelegramDeliveryResult
}
