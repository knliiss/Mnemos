package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentCreationResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentActorAccountGateway
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentMutationGateway
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentDeletionGuard
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class DeleteProfileCommentService(
    private val authors: ProfileCommentActorAccountGateway,
    private val mutations: ProfileCommentMutationGateway,
    private val guard: ProfileCommentDeletionGuard,
) : DeleteProfileCommentUseCase {
    override fun request(widget: TelegramWidgetSession, commentId: UUID): ProfileCommentRemovalResult {
        val profile = widget.selectedProfileId ?: return ProfileCommentRemovalResult.INVALID
        if (widget.screen != TelegramWidgetScreen.PROFILE_COMMENTS) return ProfileCommentRemovalResult.INVALID
        val actor = activeAccount(widget.telegramId) ?: return ProfileCommentRemovalResult.ACCOUNT_REQUIRED
        guard.request(widget.telegramId, widget.messageId, profile, commentId, actor)
        return ProfileCommentRemovalResult.CONFIRM_REQUIRED
    }

    override fun confirm(widget: TelegramWidgetSession, commentId: UUID): ProfileCommentRemovalResult {
        val profile = widget.selectedProfileId ?: return ProfileCommentRemovalResult.INVALID
        if (widget.screen != TelegramWidgetScreen.PROFILE_COMMENTS) return ProfileCommentRemovalResult.INVALID
        val actor = activeAccount(widget.telegramId) ?: return ProfileCommentRemovalResult.ACCOUNT_REQUIRED
        if (!guard.consume(widget.telegramId, widget.messageId, profile, commentId, actor)) {
            return ProfileCommentRemovalResult.NOT_CONFIRMED
        }
        return when (mutations.delete(widget.telegramId, profile, commentId, actor)) {
            is ProfileCommentCreationResult.Created -> ProfileCommentRemovalResult.APPLIED
            ProfileCommentCreationResult.AccountRequired -> ProfileCommentRemovalResult.ACCOUNT_REQUIRED
            ProfileCommentCreationResult.Conflict,
            ProfileCommentCreationResult.Invalid -> ProfileCommentRemovalResult.INVALID
            ProfileCommentCreationResult.Unavailable -> ProfileCommentRemovalResult.UNAVAILABLE
        }
    }

    override fun cancel(widget: TelegramWidgetSession): ProfileCommentRemovalResult {
        guard.cancel(widget.telegramId, widget.messageId)
        return ProfileCommentRemovalResult.CANCELLED
    }

    private fun activeAccount(telegramId: Long): UUID? =
        try {
            authors.activeAccountId(telegramId)
        } catch (_: RuntimeException) {
            null
        }
}
