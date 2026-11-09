package com.mnemos.telegram.bot.application.widget.session

import java.util.UUID

enum class TelegramWidgetScreen {
    DASHBOARD,
}

data class TelegramWidgetSession(
    val telegramId: Long,
    val messageId: Int,
    val screen: TelegramWidgetScreen,
    val accountId: UUID?,
) {
    init {
        require(telegramId > 0)
        require(messageId > 0)
    }
}
