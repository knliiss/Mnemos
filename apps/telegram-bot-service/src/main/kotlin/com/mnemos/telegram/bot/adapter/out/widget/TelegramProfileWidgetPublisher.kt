package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetView
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.presentation.widget.ProfileWidgetRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramProfileWidgetPublisher(
    private val renderer: ProfileWidgetRenderer,
    private val gateway: TelegramWidgetGateway,
    private val sessions: TelegramWidgetSessionRepository,
) : ProfileWidgetPublisher {
    override fun publish(telegramId: Long, view: ProfileWidgetView): TelegramDeliveryResult =
        when (val result = gateway.sendTracked(telegramId, renderer.render(view))) {
            is TelegramWidgetSendResult.Sent -> {
                sessions.save(TelegramWidgetSession(
                    telegramId = telegramId,
                    messageId = result.messageId,
                    screen = TelegramWidgetScreen.PROFILES,
                    accountId = null,
                    page = view.page,
                    search = view.query,
                    selectedProfileId = view.selected?.accountId,
                ))
                TelegramDeliveryResult.Delivered
            }
            TelegramWidgetSendResult.Retryable -> TelegramDeliveryResult.Retryable
            is TelegramWidgetSendResult.PermanentFailure -> TelegramDeliveryResult.PermanentFailure(result.reason)
        }

    override fun update(telegramId: Long, messageId: Int, view: ProfileWidgetView): TelegramDeliveryResult =
        gateway.updateChatData(telegramId, messageId, state(view))

    override fun state(view: ProfileWidgetView): String = renderer.state(view)
}
