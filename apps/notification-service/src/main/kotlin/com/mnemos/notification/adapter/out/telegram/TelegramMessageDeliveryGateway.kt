package com.mnemos.notification.adapter.out.telegram

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.adapter.out.telegram.TelegramMessageMapper.toReplyMarkup
import com.mnemos.notification.adapter.out.telegram.TelegramMessageMapper.toTelegram
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.configuration.telegram.TelegramBotProperties
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove
import com.pengrad.telegrambot.request.EditMessageReplyMarkup
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.response.SendResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "mnemos.notification.telegram",
    name = ["enabled"],
    havingValue = "true",
)
class TelegramMessageDeliveryGateway(
    private val bot: TelegramBot,
    private val properties: TelegramBotProperties,
) : MessageDeliveryGateway {
    override fun deliver(
        telegramId: TelegramId,
        message: RenderedMessage,
    ): DeliveryResult =
        try {
            val request = SendMessage(telegramId.id, message.text)

            message.parseMode.toTelegram()?.let {
                request.parseMode(it)
            }

            val replyMarkup = message.toReplyMarkup(properties.miniAppUrl)

            if (replyMarkup is InlineKeyboardMarkup) {
                deliverWithLegacyReplyKeyboardRemoval(
                    telegramId = telegramId,
                    request = request,
                    replyMarkup = replyMarkup,
                )
            } else {
                replyMarkup?.let {
                    request.replyMarkup(it)
                }

                bot.execute(request).toDeliveryResult()
            }
        } catch (_: Exception) {
            DeliveryResult.Retryable()
        }

    private fun deliverWithLegacyReplyKeyboardRemoval(
        telegramId: TelegramId,
        request: SendMessage,
        replyMarkup: InlineKeyboardMarkup,
    ): DeliveryResult {
        request.replyMarkup(ReplyKeyboardRemove())

        val sendResponse: SendResponse = bot.execute(request)
        val sendResult = sendResponse.toDeliveryResult()

        if (sendResult != DeliveryResult.Delivered) {
            return sendResult
        }

        val messageId = sendResponse.message()?.messageId()
            ?: return DeliveryResult.Retryable()
        val editRequest =
            EditMessageReplyMarkup(telegramId.id, messageId)
                .replyMarkup(replyMarkup)
        val editResponse: BaseResponse = bot.execute(editRequest)

        return editResponse.toDeliveryResult()
    }

    private fun BaseResponse.toDeliveryResult(): DeliveryResult =
        when {
            isOk() ->
                DeliveryResult.Delivered

            errorCode() == 403 ->
                DeliveryResult.PermanentFailure(
                    "TELEGRAM_BLOCKED",
                )

            errorCode() == 429 ->
                DeliveryResult.Retryable()

            errorCode() >= 500 ->
                DeliveryResult.Retryable()

            else ->
                DeliveryResult.PermanentFailure(
                    description() ?: "Telegram delivery failed",
                )
        }
}
