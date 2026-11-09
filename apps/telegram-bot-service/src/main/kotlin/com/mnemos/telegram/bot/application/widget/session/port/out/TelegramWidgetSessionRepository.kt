package com.mnemos.telegram.bot.application.widget.session.port.out

import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession

interface TelegramWidgetSessionRepository {
    fun find(
        telegramId: Long,
        messageId: Int,
    ): TelegramWidgetSession?

    fun save(session: TelegramWidgetSession)
}
