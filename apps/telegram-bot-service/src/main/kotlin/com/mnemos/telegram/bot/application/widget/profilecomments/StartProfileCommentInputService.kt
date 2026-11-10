package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentActorAccountGateway
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextPromptGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class StartProfileCommentInputService(
    private val properties: TelegramWidgetProperties,
    private val actors: ProfileCommentActorAccountGateway,
    private val prompts: WidgetTextPromptGateway,
    private val sessions: WidgetTextInputSessionRepository,
) : StartProfileCommentInputUseCase {
    override fun execute(widget: TelegramWidgetSession): TelegramDeliveryResult =
        start(widget, null)

    override fun edit(widget: TelegramWidgetSession, commentId: UUID): TelegramDeliveryResult =
        start(widget, commentId)

    private fun start(widget: TelegramWidgetSession, editedCommentId: UUID?): TelegramDeliveryResult {
        if (!properties.enabled || widget.screen != TelegramWidgetScreen.PROFILE_COMMENTS) {
            return TelegramDeliveryResult.PermanentFailure("widget-input-unavailable")
        }
        val targetProfileId = widget.selectedProfileId
            ?: return TelegramDeliveryResult.PermanentFailure("profile-not-selected")
        val actorAccountId = try {
            actors.activeAccountId(widget.telegramId)
        } catch (_: RuntimeException) {
            return TelegramDeliveryResult.Retryable
        } ?: return TelegramDeliveryResult.PermanentFailure("active-account-required")

        val purpose = when {
            editedCommentId != null -> WidgetTextInputPurpose.PROFILE_COMMENT_EDIT
            widget.cursor == null -> WidgetTextInputPurpose.PROFILE_COMMENT_CREATE
            else -> WidgetTextInputPurpose.PROFILE_COMMENT_REPLY
        }
        return try {
            when (val sent = prompts.prompt(widget.telegramId, widget.messageId, purpose)) {
                is TelegramMessageSendResult.Delivered -> {
                    sessions.save(
                        WidgetTextInputSession(
                            telegramId = widget.telegramId,
                            promptMessageId = sent.messageId,
                            widgetMessageId = widget.messageId,
                            accountId = widget.accountId,
                            purpose = purpose,
                            targetProfileId = targetProfileId,
                            targetCommentId = editedCommentId ?: widget.cursor,
                            expectedActorAccountId = actorAccountId,
                            mutationId = UUID.randomUUID(),
                            threadRootId = if (editedCommentId != null) widget.cursor else null,
                        ),
                    )
                    TelegramDeliveryResult.Delivered
                }
                TelegramMessageSendResult.Retryable -> TelegramDeliveryResult.Retryable
                is TelegramMessageSendResult.PermanentFailure -> TelegramDeliveryResult.PermanentFailure(sent.reason)
            }
        } catch (_: RuntimeException) {
            TelegramDeliveryResult.Retryable
        }
    }
}
