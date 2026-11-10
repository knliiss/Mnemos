package com.mnemos.telegram.bot.application.widget.profileactivity

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import java.util.UUID

interface OpenProfileActivityWidgetUseCase {
    val enabled: Boolean

    fun execute(telegramId: Long, profileId: UUID): TelegramDeliveryResult
}
