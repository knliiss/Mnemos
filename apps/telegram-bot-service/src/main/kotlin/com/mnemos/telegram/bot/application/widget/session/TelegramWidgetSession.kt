package com.mnemos.telegram.bot.application.widget.session

import java.util.UUID

data class TelegramWidgetSession(
    val telegramId: Long,
    val messageId: Int,
    val screen: TelegramWidgetScreen,
    val accountId: UUID?,
    val page: Int = 0,
    val cursor: UUID? = null,
    val search: String? = null,
) {
    init {
        require(telegramId > 0)
        require(messageId > 0)
        require(page in 0..1_000)
        require(search == null || (search.isNotBlank() && search.length <= 64 && search.toByteArray(Charsets.UTF_8).size <= 256))
    }
}
