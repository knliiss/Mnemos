package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession

interface ApplyWidgetAccountInputUseCase {
    fun apply(
        prompt: WidgetTextInputSession,
        widget: TelegramWidgetSession,
        text: String,
    ): TelegramDeliveryResult
}
