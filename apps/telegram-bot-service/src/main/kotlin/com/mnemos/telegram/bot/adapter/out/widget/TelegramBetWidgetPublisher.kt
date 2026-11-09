package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.bets.BetWidgetView
import com.mnemos.telegram.bot.application.widget.bets.port.out.BetWidgetPublisher
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.presentation.widget.BetWidgetRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramBetWidgetPublisher(
    private val renderer: BetWidgetRenderer,
    private val gateway: TelegramWidgetGateway,
    private val sessions: TelegramWidgetSessionRepository,
) : BetWidgetPublisher {
    override fun publish(
        telegramId: Long,
        view: BetWidgetView,
    ): TelegramDeliveryResult =
        when (val result = gateway.sendTracked(telegramId, renderer.render(view))) {
            is TelegramWidgetSendResult.Sent -> {
                sessions.save(
                    TelegramWidgetSession(
                        telegramId = telegramId,
                        messageId = result.messageId,
                        screen = TelegramWidgetScreen.BETS,
                        accountId = view.accountId,
                        page = view.mode.ordinal,
                    ),
                )
                TelegramDeliveryResult.Delivered
            }

            TelegramWidgetSendResult.Retryable -> TelegramDeliveryResult.Retryable
            is TelegramWidgetSendResult.PermanentFailure -> TelegramDeliveryResult.PermanentFailure(result.reason)
        }

    override fun update(
        telegramId: Long,
        messageId: Int,
        view: BetWidgetView,
    ): TelegramDeliveryResult = gateway.updateChatData(telegramId, messageId, state(view))

    override fun state(view: BetWidgetView): String = renderer.state(view)
}
