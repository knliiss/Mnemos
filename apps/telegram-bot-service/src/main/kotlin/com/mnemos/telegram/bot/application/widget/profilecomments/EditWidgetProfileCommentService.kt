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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class EditWidgetProfileCommentService(
    private val authors: ProfileCommentActorAccountGateway,
    private val mutations: ProfileCommentMutationGateway,
    private val snapshots: ProfileCommentsWidgetSnapshotQuery,
    private val publisher: ProfileCommentsWidgetPublisher,
) : EditWidgetProfileCommentUseCase {
    override fun edit(
        input: WidgetTextInputSession,
        widget: TelegramWidgetSession,
        content: String,
    ): TelegramDeliveryResult {
        if (widget.screen != TelegramWidgetScreen.PROFILE_COMMENTS ||
            input.purpose != WidgetTextInputPurpose.PROFILE_COMMENT_EDIT ||
            input.telegramId != widget.telegramId ||
            input.widgetMessageId != widget.messageId ||
            input.accountId != widget.accountId ||
            input.targetProfileId == null ||
            input.targetProfileId != widget.selectedProfileId ||
            input.targetCommentId == null ||
            input.threadRootId != widget.cursor ||
            input.expectedActorAccountId == null
        ) return TelegramDeliveryResult.PermanentFailure("invalid-comment-edit-context")

        val active = try {
            authors.activeAccountId(input.telegramId)
        } catch (_: RuntimeException) {
            return TelegramDeliveryResult.Retryable
        }
        if (active != input.expectedActorAccountId) {
            return TelegramDeliveryResult.PermanentFailure("comment-author-account-changed")
        }

        val current = (snapshots.load(widget.telegramId, input.targetProfileId, widget.page, widget.cursor)
            as? ProfileCommentsWidgetSnapshotResult.Available)?.view
            ?: return TelegramDeliveryResult.Retryable
        if (current.comments.none { it.id == input.targetCommentId && it.canEdit && !it.deleted }) {
            return TelegramDeliveryResult.PermanentFailure("comment-edit-target-not-authorized")
        }

        val body = content.trim()
        if (body.isEmpty() || body.length > 500) {
            return publisher.update(
                widget.telegramId,
                widget.messageId,
                current.copy(feedback = "Текст коментаря має містити від 1 до 500 символів"),
            )
        }

        return when (
            mutations.edit(
                input.telegramId,
                input.targetProfileId,
                input.targetCommentId,
                input.expectedActorAccountId,
                body,
            )
        ) {
            is ProfileCommentCreationResult.Created -> {
                val fresh = (snapshots.load(widget.telegramId, input.targetProfileId, widget.page, widget.cursor)
                    as? ProfileCommentsWidgetSnapshotResult.Available)?.view
                    ?: return TelegramDeliveryResult.Retryable
                publisher.update(
                    widget.telegramId,
                    widget.messageId,
                    fresh.copy(feedback = "Коментар змінено"),
                )
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
