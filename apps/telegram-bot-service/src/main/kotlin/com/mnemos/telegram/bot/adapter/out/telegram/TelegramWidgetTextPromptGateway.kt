package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextPromptGateway
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import tools.jackson.databind.JsonNode
import java.time.Duration

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramWidgetTextPromptGateway(
    properties: TelegramBotProperties,
) : WidgetTextPromptGateway {
    private val client =
        RestClient.builder()
            .baseUrl("https://api.telegram.org/bot" + properties.token)
            .requestFactory(JdkClientHttpRequestFactory().apply { setReadTimeout(Duration.ofSeconds(10)) })
            .build()

    override fun prompt(
        telegramId: Long,
        widgetMessageId: Int,
        purpose: WidgetTextInputPurpose,
    ): TelegramMessageSendResult {
        val (text, placeholder) =
            when (purpose) {
                WidgetTextInputPurpose.EVENT_SEARCH ->
                    "Введіть запит для пошуку подій. Відповідайте на це повідомлення або напишіть «скасувати»." to
                        "Пошук подій"
                WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH ->
                    "Введіть нікнейм гравця. Відповідайте на це повідомлення або напишіть «скасувати»." to
                        "Нікнейм гравця"
                WidgetTextInputPurpose.ACCOUNT_CREATE ->
                    "Введіть нікнейм Cristalix для створення акаунта. Відповідайте на це повідомлення або напишіть «скасувати»." to
                        "Нікнейм Cristalix"
                WidgetTextInputPurpose.ACCOUNT_JOIN ->
                    "Введіть код запрошення до акаунта. Відповідайте на це повідомлення або напишіть «скасувати»." to
                        "Код запрошення"
            }

        return try {
            val response = client.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "chat_id" to telegramId,
                        "text" to text,
                        "reply_parameters" to mapOf("message_id" to widgetMessageId),
                        "reply_markup" to mapOf(
                            "force_reply" to true,
                            "input_field_placeholder" to placeholder,
                        ),
                    ),
                )
                .retrieve()
                .body(JsonNode::class.java) ?: error("Empty Telegram response")
            val messageId = response.path("result").path("message_id").asInt(0)
            if (response.path("ok").asBoolean(false) && messageId > 0) {
                TelegramMessageSendResult.Delivered(messageId)
            } else {
                TelegramMessageSendResult.Retryable
            }
        } catch (exception: RestClientResponseException) {
            if (exception.statusCode.value() in 400..499 && exception.statusCode.value() != 429) {
                TelegramMessageSendResult.PermanentFailure("widget-input-prompt-rejected")
            } else {
                TelegramMessageSendResult.Retryable
            }
        } catch (_: Exception) {
            TelegramMessageSendResult.Retryable
        }
    }
}
