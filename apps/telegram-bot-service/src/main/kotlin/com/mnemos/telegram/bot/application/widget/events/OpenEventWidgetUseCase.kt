package com.mnemos.telegram.bot.application.widget.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import java.util.UUID

interface OpenEventWidgetUseCase {
    val enabled: Boolean

    fun execute(
        telegramId: Long,
        filter: EventHistoryFilter = EventHistoryFilter.ALL,
        after: UUID? = null,
        search: String? = null,
    ): TelegramDeliveryResult
}
