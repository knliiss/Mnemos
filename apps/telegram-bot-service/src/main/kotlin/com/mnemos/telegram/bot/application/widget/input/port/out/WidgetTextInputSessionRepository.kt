package com.mnemos.telegram.bot.application.widget.input.port.out

import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession

interface WidgetTextInputSessionRepository {
    fun find(telegramId: Long, promptMessageId: Int): WidgetTextInputSession?

    fun save(session: WidgetTextInputSession)

    fun delete(telegramId: Long, promptMessageId: Int)
}
