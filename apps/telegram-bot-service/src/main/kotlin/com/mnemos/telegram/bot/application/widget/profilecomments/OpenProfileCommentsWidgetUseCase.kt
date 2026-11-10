package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import java.util.UUID

fun interface OpenProfileCommentsWidgetUseCase {
    fun execute(telegramId: Long, profileId: UUID): TelegramDeliveryResult
}
