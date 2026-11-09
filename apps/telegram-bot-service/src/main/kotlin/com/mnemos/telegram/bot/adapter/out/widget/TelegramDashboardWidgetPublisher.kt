package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import com.mnemos.telegram.bot.application.widget.dashboard.port.out.DashboardWidgetPublisher
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.presentation.widget.DashboardWidgetRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramDashboardWidgetPublisher(
    private val renderer: DashboardWidgetRenderer,
    private val gateway: TelegramWidgetGateway,
) : DashboardWidgetPublisher {
    override fun publish(
        telegramId: Long,
        view: DashboardWidgetView,
    ): TelegramDeliveryResult = gateway.send(telegramId, renderer.render(view))
}
