package com.mnemos.notification.adapter.out.telegram

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.MessageParseMode
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.configuration.telegram.TelegramBotProperties
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.WebAppInfo
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
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

            message.toReplyMarkup()?.let {
                request.replyMarkup(it)
            }

            val response: SendResponse = bot.execute(request)

            when {
                response.isOk() ->
                    DeliveryResult.Delivered

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

    private fun MessageParseMode.toTelegram(): ParseMode? =
        when (this) {
            MessageParseMode.HTML -> ParseMode.HTML
            MessageParseMode.MARKDOWN_V2 -> ParseMode.MarkdownV2
            MessageParseMode.PLAIN -> null
        }

    private fun RenderedMessage.toReplyMarkup(): Keyboard? {
        if (actions.isEmpty()) {
            return null
        }

        val buttons =
            actions
                .map { action ->
                    when (action) {
                        is MessageAction.OpenMiniApp ->
                            KeyboardButton(action.label)
                                .webAppInfo(WebAppInfo(properties.miniAppUrl))
                    }
                }.toTypedArray()

        return ReplyKeyboardMarkup(*buttons)
            .resizeKeyboard(true)
    }
}
