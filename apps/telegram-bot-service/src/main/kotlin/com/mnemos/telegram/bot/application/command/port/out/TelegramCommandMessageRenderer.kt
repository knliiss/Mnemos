package com.mnemos.telegram.bot.application.command.port.out

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage

interface TelegramCommandMessageRenderer {
    fun renderStart(context: TelegramStartContext): OutboundTelegramMessage

    fun renderHelp(section: TelegramHelpSection): OutboundTelegramMessage
}
