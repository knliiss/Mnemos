package com.mnemos.telegram.bot.application.widget.eventdetail.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.eventdetail.EventDetailView

fun interface EventDetailWidgetPublisher {
    fun publish(telegramId: Long, view: EventDetailView): TelegramDeliveryResult
}
