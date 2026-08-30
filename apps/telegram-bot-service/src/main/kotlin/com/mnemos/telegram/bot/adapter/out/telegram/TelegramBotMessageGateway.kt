package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.adapter.out.telegram.TelegramDeliveryResultMapper.toDeliveryResult
import com.mnemos.telegram.bot.adapter.out.telegram.TelegramMessageMapper.toReplyMarkup
import com.mnemos.telegram.bot.adapter.out.telegram.TelegramMessageMapper.toTelegram
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.EditMessageText
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramBotMessageGateway(
    private val bot: TelegramBot,
    private val properties: TelegramBotProperties,
) : TelegramMessageGateway {
    override fun send(
        telegramId: Long,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult =
        try {
            val request = SendMessage(telegramId, message.text)

            message.parseMode.toTelegram()?.let(request::parseMode)
            message.toReplyMarkup(properties.miniAppUrl)?.let(request::replyMarkup)

            bot.execute(request).toDeliveryResult()
        } catch (_: Exception) {
            TelegramDeliveryResult.Retryable
        }

    override fun edit(
        telegramId: Long,
        messageId: Int,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult =
        try {
            val request = EditMessageText(telegramId, messageId, message.text)

            message.parseMode.toTelegram()?.let(request::parseMode)

            val replyMarkup = message.toReplyMarkup(properties.miniAppUrl)

            if (replyMarkup is InlineKeyboardMarkup) {
                request.replyMarkup(replyMarkup)
            }

            bot.execute(request).toDeliveryResult()
        } catch (_: Exception) {
            TelegramDeliveryResult.Retryable
        }
}
