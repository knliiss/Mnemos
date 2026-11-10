package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.application.widget.wallet.WalletWidgetView
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletWidgetPublisher
import com.mnemos.telegram.bot.presentation.widget.WalletWidgetRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramWalletWidgetPublisher(
    private val renderer: WalletWidgetRenderer,
    private val gateway: TelegramWidgetGateway,
    private val sessions: TelegramWidgetSessionRepository,
) : WalletWidgetPublisher {
    override fun publish(
        telegramId: Long,
        view: WalletWidgetView,
    ): TelegramDeliveryResult =
        when (val result = gateway.sendTracked(telegramId, renderer.render(view))) {
            is TelegramWidgetSendResult.Sent -> {
                sessions.save(
                    TelegramWidgetSession(
                        telegramId = telegramId,
                        messageId = result.messageId,
                        screen = TelegramWidgetScreen.WALLET,
                        accountId = view.accountId,
                        page = view.page,
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
        view: WalletWidgetView,
    ): TelegramDeliveryResult = gateway.updateChatData(telegramId, messageId, state(view))

    override fun state(view: WalletWidgetView): String = renderer.state(view)
}
