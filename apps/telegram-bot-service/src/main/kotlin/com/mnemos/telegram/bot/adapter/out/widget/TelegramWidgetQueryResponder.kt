package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

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
        val payload =
            if (state == null) {
                json.writeValueAsString(mapOf("status" to status, "message" to message))
            } else {
                json.writeValueAsString(
                    mapOf("status" to status, "message" to message, "state" to json.readTree(state)),
                )
            }
        return gateway.answerQuery(queryId, payload)
    }
}
