package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import tools.jackson.databind.JsonNode
import java.nio.charset.StandardCharsets
import java.time.Duration

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramWidgetHttpGateway(
    properties: TelegramBotProperties,
) : TelegramWidgetGateway {
    private val client =
        RestClient
            .builder()
            .baseUrl("https://api.telegram.org/bot" + properties.token)
            .requestFactory(
                JdkClientHttpRequestFactory().apply {
                    setReadTimeout(Duration.ofSeconds(10))
                },
            ).build()

    override fun sendTracked(
        telegramId: Long,
        widget: TelegramWidgetMessage,
    ): TelegramWidgetSendResult =
        try {
            val response = post("sendWidget", TelegramWidgetRequestMapper.toSendBody(telegramId, widget))
            val messageId = response.path("result").path("message_id").asInt(0)

            if (response.path("ok").asBoolean(false) && messageId > 0) {
                TelegramWidgetSendResult.Sent(messageId)
            } else {
                TelegramWidgetSendResult.Retryable
            }
        } catch (exception: RestClientResponseException) {
            if (exception.statusCode.value() in 400..499 && exception.statusCode.value() != 429) {
                TelegramWidgetSendResult.PermanentFailure("telegram-widget-rejected")
            } else {
                TelegramWidgetSendResult.Retryable
            }
        } catch (_: Exception) {
            TelegramWidgetSendResult.Retryable
        }

    override fun answerQuery(
        queryId: String,
        data: String,
    ): TelegramDeliveryResult {
        require(data.toByteArray(StandardCharsets.UTF_8).size <= 1_024)
        return perform("answerWidgetQuery", mapOf("widget_query_id" to queryId, "data" to data))
    }

    override fun updateChatData(
        telegramId: Long,
        messageId: Int,
        data: String,
    ): TelegramDeliveryResult {
        require(data.toByteArray(StandardCharsets.UTF_8).size <= 1_024)
        return perform(
            "editMessageWidgetData",
            mapOf("chat_id" to telegramId, "message_id" to messageId, "chat_data" to data),
        )
    }

    private fun perform(
        method: String,
        body: Map<String, Any>,
    ): TelegramDeliveryResult =
        try {
            val response = post(method, body)
            if (response.path("ok").asBoolean(false)) {
                TelegramDeliveryResult.Delivered
            } else {
                TelegramDeliveryResult.Retryable
            }
        } catch (exception: RestClientResponseException) {
            when {
                method == "editMessageWidgetData" &&
                    exception.statusCode.value() == 400 &&
                    exception.responseBodyAsString.contains("message is not modified", ignoreCase = true) ->
                    TelegramDeliveryResult.Delivered
                exception.statusCode.value() in 400..499 && exception.statusCode.value() != 429 ->
                    TelegramDeliveryResult.PermanentFailure("telegram-widget-rejected")
                else -> TelegramDeliveryResult.Retryable
            }
        } catch (_: Exception) {
            TelegramDeliveryResult.Retryable
        }

    private fun post(
        method: String,
        body: Map<String, Any>,
    ): JsonNode =
        client
            .post()
            .uri("/" + method)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(JsonNode::class.java) ?: error("Missing Telegram response")
}
