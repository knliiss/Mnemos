package com.mnemos.telegram.bot.application.menu.port.out

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession

interface TelegramMenuSessionRepository {
    fun find(telegramId: Long): TelegramMenuSession?

    fun save(session: TelegramMenuSession)
}
