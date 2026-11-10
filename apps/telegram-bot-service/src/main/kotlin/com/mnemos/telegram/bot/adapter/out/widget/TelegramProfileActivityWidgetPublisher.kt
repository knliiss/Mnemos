package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.profileactivity.ProfileActivityView
import com.mnemos.telegram.bot.application.widget.profileactivity.port.out.ProfileActivityWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.presentation.widget.ProfileActivityWidgetRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramProfileActivityWidgetPublisher(
    private val renderer: ProfileActivityWidgetRenderer,
    private val gateway: TelegramWidgetGateway,
    private val sessions: TelegramWidgetSessionRepository,
) : ProfileActivityWidgetPublisher {
    override fun publish(telegramId: Long, view: ProfileActivityView): TelegramDeliveryResult =
        when (val result = gateway.sendTracked(telegramId, renderer.render(view))) {
            is TelegramWidgetSendResult.Sent -> {
                sessions.save(
                    TelegramWidgetSession(
                        telegramId = telegramId,
                        messageId = result.messageId,
                        screen = TelegramWidgetScreen.PROFILE_ACTIVITY,
                        accountId = null,
                        cursor = view.after,
                        selectedProfileId = view.profileId,
                    ),
                )
                TelegramDeliveryResult.Delivered
            }
            TelegramWidgetSendResult.Retryable -> TelegramDeliveryResult.Retryable
            is TelegramWidgetSendResult.PermanentFailure -> TelegramDeliveryResult.PermanentFailure(result.reason)
        }

    override fun update(telegramId: Long, messageId: Int, view: ProfileActivityView): TelegramDeliveryResult =
        gateway.updateChatData(telegramId, messageId, state(view))

    override fun state(view: ProfileActivityView): String = renderer.state(view)
}
