package com.mnemos.telegram.bot.application.widget.session

import java.util.UUID

enum class TelegramWidgetScreen {
    DASHBOARD,
    NOTIFICATIONS,
}

data class TelegramWidgetSession(
    val telegramId: Long,
    val messageId: Int,
    val screen: TelegramWidgetScreen,
    val accountId: UUID?,
    val page: Int = 0,
) {
    init {
        require(telegramId > 0)
        require(messageId > 0)
        require(page in 0..1_000)
    }
}
