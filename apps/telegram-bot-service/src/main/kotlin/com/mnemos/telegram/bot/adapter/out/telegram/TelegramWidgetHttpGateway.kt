package com.mnemos.telegram.bot.adapter.out.telegram

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
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

    override fun send(
        telegramId: Long,
        widget: TelegramWidgetMessage,
    ): TelegramDeliveryResult {
        val body = TelegramWidgetRequestMapper.toSendBody(telegramId, widget)

        return try {
            val result =
                client
                    .post()
                    .uri("/sendWidget")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode::class.java)

            if (result?.path("ok")?.asBoolean(false) == true) {
                TelegramDeliveryResult.Delivered
            } else {
                TelegramDeliveryResult.Retryable
            }
        } catch (exception: RestClientResponseException) {
            if (exception.statusCode.value() in 400..499 && exception.statusCode.value() != 429) {
                TelegramDeliveryResult.PermanentFailure("telegram-widget-rejected")
            } else {
                TelegramDeliveryResult.Retryable
            }
        } catch (_: Exception) {
            TelegramDeliveryResult.Retryable
        }
    }
}
