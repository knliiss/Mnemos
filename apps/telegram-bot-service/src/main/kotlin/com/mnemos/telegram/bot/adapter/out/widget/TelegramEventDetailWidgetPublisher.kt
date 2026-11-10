package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.eventdetail.EventDetailView
import com.mnemos.telegram.bot.application.widget.eventdetail.port.out.EventDetailWidgetPublisher
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.presentation.widget.EventDetailWidgetRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramEventDetailWidgetPublisher(
    private val renderer: EventDetailWidgetRenderer,
    private val telegram: TelegramWidgetGateway,
) : EventDetailWidgetPublisher {
    override fun publish(telegramId: Long, view: EventDetailView): TelegramDeliveryResult =
        telegram.send(telegramId, renderer.render(view))
}
