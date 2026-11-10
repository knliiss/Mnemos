package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentCreationResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentActorAccountGateway
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentMutationGateway
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentsWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class CreateWidgetProfileCommentService(
    private val authors: ProfileCommentActorAccountGateway,
    private val mutations: ProfileCommentMutationGateway,
    private val snapshots: ProfileCommentsWidgetSnapshotQuery,
    private val publisher: ProfileCommentsWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
) : CreateWidgetProfileCommentUseCase {
    override fun create(
        input: WidgetTextInputSession,
        widget: TelegramWidgetSession,
        content: String,
    ): TelegramDeliveryResult {
        val isCommentInput = input.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_CREATE ||
            input.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_REPLY
        if (!isCommentInput ||
            widget.screen != TelegramWidgetScreen.PROFILE_COMMENTS ||
            input.telegramId != widget.telegramId ||
            input.widgetMessageId != widget.messageId ||
            input.accountId != widget.accountId ||
            input.targetProfileId == null ||
            input.targetProfileId != widget.selectedProfileId ||
            input.targetCommentId != widget.cursor ||
            input.expectedActorAccountId == null ||
            input.mutationId == null ||
            (input.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_CREATE && input.targetCommentId != null) ||
            (input.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_REPLY && input.targetCommentId == null)
        ) return TelegramDeliveryResult.PermanentFailure("invalid-comment-input-context")

        val active = try {
            authors.activeAccountId(input.telegramId)
        } catch (_: RuntimeException) {
            return TelegramDeliveryResult.Retryable
        }
        if (active != input.expectedActorAccountId) {
            return TelegramDeliveryResult.PermanentFailure("comment-author-account-changed")
        }

        val body = content.trim()
        if (body.isEmpty() || body.length > 500) {
            val current = (snapshots.load(widget.telegramId, input.targetProfileId, widget.page, widget.cursor)
                as? ProfileCommentsWidgetSnapshotResult.Available)?.view
                ?: return TelegramDeliveryResult.Retryable
            return publisher.update(
                widget.telegramId,
                widget.messageId,
                current.copy(feedback = "Коментар має містити від 1 до 500 символів"),
            )
        }

        return when (
            mutations.create(
                input.telegramId,
                input.targetProfileId,
                input.expectedActorAccountId,
                input.mutationId,
                input.targetCommentId,
                body,
            )
        ) {
            is ProfileCommentCreationResult.Created -> {
                val fresh = (snapshots.load(widget.telegramId, input.targetProfileId, 0, input.targetCommentId)
                    as? ProfileCommentsWidgetSnapshotResult.Available)?.view
                    ?: return TelegramDeliveryResult.Retryable
                val result = publisher.update(
                    widget.telegramId,
                    widget.messageId,
                    fresh.copy(feedback = "Коментар опубліковано"),
                )
                if (result == TelegramDeliveryResult.Delivered) {
                    sessions.save(widget.copy(page = 0))
                }
                result
            }
            ProfileCommentCreationResult.AccountRequired ->
                TelegramDeliveryResult.PermanentFailure("comment-active-account-required")
            ProfileCommentCreationResult.Conflict ->
                TelegramDeliveryResult.PermanentFailure("comment-operation-conflict")
            ProfileCommentCreationResult.Invalid ->
                TelegramDeliveryResult.PermanentFailure("comment-invalid")
            ProfileCommentCreationResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }
}
