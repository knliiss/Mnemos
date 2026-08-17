package com.mnemos.notification.adapter.out.telegram

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.adapter.out.telegram.TelegramMessageMapper.toReplyMarkup
import com.mnemos.notification.adapter.out.telegram.TelegramMessageMapper.toTelegram
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.configuration.telegram.TelegramBotProperties
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
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

            message.toReplyMarkup(properties.miniAppUrl)?.let {
                request.replyMarkup(it)
            }

            val response: SendResponse = bot.execute(request)

            when {
                response.isOk() ->
                    DeliveryResult.Delivered

                response.errorCode() == 403 ->
                    DeliveryResult.PermanentFailure(
                        "TELEGRAM_BLOCKED",
                    )

                response.errorCode() == 429 ->
                    DeliveryResult.Retryable()

                response.errorCode() >= 500 ->
                    DeliveryResult.Retryable()

                else ->
                    DeliveryResult.PermanentFailure(
                        response.description() ?: "Telegram delivery failed",
                    )
            }
        } catch (_: Exception) {
            DeliveryResult.Retryable()
        }
}
