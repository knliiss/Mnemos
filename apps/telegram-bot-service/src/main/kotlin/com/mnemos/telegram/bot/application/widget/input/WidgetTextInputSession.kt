package com.mnemos.telegram.bot.application.widget.input

import java.util.UUID

data class WidgetTextInputSession(
    val telegramId: Long,
    val promptMessageId: Int,
    val widgetMessageId: Int,
    val accountId: UUID?,
    val purpose: WidgetTextInputPurpose,
    val targetRoleId: UUID? = null,
    val targetProfileId: UUID? = null,
    val targetCommentId: UUID? = null,
    val expectedActorAccountId: UUID? = null,
    val mutationId: UUID? = null,
    val threadRootId: UUID? = null,
) {
    init {
        require(telegramId > 0)
        require(promptMessageId > 0)
        require(widgetMessageId > 0)
    }
}
