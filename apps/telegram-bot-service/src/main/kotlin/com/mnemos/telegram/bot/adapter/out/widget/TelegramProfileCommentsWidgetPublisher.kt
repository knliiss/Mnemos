package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.profilecomments.ProfileCommentsWidgetView
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentsWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.presentation.widget.ProfileCommentsWidgetRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramProfileCommentsWidgetPublisher(
    private val renderer: ProfileCommentsWidgetRenderer,
    private val gateway: TelegramWidgetGateway,
    private val sessions: TelegramWidgetSessionRepository,
) : ProfileCommentsWidgetPublisher {
    override fun publish(telegramId: Long, view: ProfileCommentsWidgetView): TelegramDeliveryResult =
        when (val result = gateway.sendTracked(telegramId, renderer.render(view))) {
            is TelegramWidgetSendResult.Sent -> {
                sessions.save(
                    TelegramWidgetSession(
                        telegramId = telegramId,
                        messageId = result.messageId,
                        screen = TelegramWidgetScreen.PROFILE_COMMENTS,
                        accountId = null,
                        page = view.page,
                        cursor = view.rootCommentId,
                        selectedProfileId = view.profileId,
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
        view: ProfileCommentsWidgetView,
    ): TelegramDeliveryResult = gateway.updateChatData(telegramId, messageId, state(view))

    override fun state(view: ProfileCommentsWidgetView): String = renderer.state(view)
}
