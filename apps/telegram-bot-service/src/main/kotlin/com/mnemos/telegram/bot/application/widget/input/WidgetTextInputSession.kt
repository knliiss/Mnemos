package com.mnemos.telegram.bot.application.widget.input

import java.util.UUID

data class WidgetTextInputSession(
    val telegramId: Long,
    val promptMessageId: Int,
    val widgetMessageId: Int,
    val accountId: UUID?,
    val purpose: WidgetTextInputPurpose,
) {
    init {
        require(telegramId > 0)
        require(promptMessageId > 0)
        require(widgetMessageId > 0)
    }
}
