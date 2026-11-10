package com.mnemos.telegram.bot.application.widget.input.port.out

import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession

interface WidgetTextInputSessionRepository {
    fun find(telegramId: Long, promptMessageId: Int): WidgetTextInputSession?

    fun save(session: WidgetTextInputSession)

    fun claimMutation(telegramId: Long, promptMessageId: Int): Boolean

    fun delete(telegramId: Long, promptMessageId: Int)
}
