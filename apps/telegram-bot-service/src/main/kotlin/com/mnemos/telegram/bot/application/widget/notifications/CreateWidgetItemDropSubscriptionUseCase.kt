package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession

interface CreateWidgetItemDropSubscriptionUseCase {
    fun create(prompt: WidgetTextInputSession, widget: TelegramWidgetSession, text: String): TelegramDeliveryResult
}
