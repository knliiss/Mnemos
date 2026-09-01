package com.mnemos.telegram.bot.application.command.port.out

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage

interface TelegramCommandMessageRenderer {
    fun renderStart(
        context: TelegramStartContext,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage

    fun renderHelp(
        section: TelegramHelpSection,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage
}
