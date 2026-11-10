package com.mnemos.telegram.bot.application.widget.eventdetail

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import java.util.UUID

fun interface OpenEventDetailWidgetUseCase {
    fun execute(telegramId: Long, eventId: UUID): TelegramDeliveryResult
}
