package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramWidgetQueryResponder(
    private val json: ObjectMapper,
    private val gateway: TelegramWidgetGateway,
) : WidgetQueryResponder {
    override fun reply(
        queryId: String,
        status: String,
        message: String,
        state: String?,
    ): TelegramDeliveryResult {
        val summary = mapOf("status" to status, "message" to message)
        val fallback = json.writeValueAsString(summary)
        val expanded =
            if (state == null) {
                fallback
            } else {
                json.writeValueAsString(
                    mapOf("status" to status, "message" to message, "state" to json.readTree(state)),
                )
            }
        val payload =
            if (expanded.toByteArray(StandardCharsets.UTF_8).size <= MAX_RESPONSE_BYTES) {
                expanded
            } else {
                fallback
            }
        require(payload.toByteArray(StandardCharsets.UTF_8).size <= MAX_RESPONSE_BYTES)
        return gateway.answerQuery(queryId, payload)
    }

    private companion object {
        const val MAX_RESPONSE_BYTES = 1_024
    }
}
