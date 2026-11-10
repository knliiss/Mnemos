package com.mnemos.telegram.bot.application.widget.invites

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface OpenInviteWidgetUseCase {
    val enabled: Boolean

    fun execute(telegramId: Long, page: Int = 0): TelegramDeliveryResult
}
