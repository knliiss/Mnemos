package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession

interface ApplyWidgetTextSearchUseCase {
    fun apply(session: TelegramWidgetSession, search: String): TelegramDeliveryResult
}
