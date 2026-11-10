package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import java.util.UUID

interface DeleteProfileCommentUseCase {
    fun request(widget: TelegramWidgetSession, commentId: UUID): ProfileCommentRemovalResult
    fun confirm(widget: TelegramWidgetSession, commentId: UUID): ProfileCommentRemovalResult
    fun cancel(widget: TelegramWidgetSession): ProfileCommentRemovalResult
}
