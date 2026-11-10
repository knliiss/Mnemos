package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession

fun interface CreateWidgetAccountRoleUseCase {
    fun create(prompt: WidgetTextInputSession, widget: TelegramWidgetSession, text: String): TelegramDeliveryResult
}
