package com.mnemos.event.adapter.inbound.collector

import com.mnemos.event.adapter.inbound.collector.dto.CollectorClientMessage
import com.mnemos.event.adapter.inbound.collector.dto.CollectorEventReportMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper

@Component
class CollectorMessageCodec(
    jsonMapper: JsonMapper,
) {
    private val strictJsonMapper =
        jsonMapper
            .rebuild()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

    fun decode(payload: String): CollectorClientMessage =
        strictJsonMapper.readValue(
            payload,
            CollectorClientMessage::class.java,
        )

    fun decodeReport(payload: String): CollectorEventReportMessage =
        strictJsonMapper.readValue(
            payload,
            CollectorEventReportMessage::class.java,
        )

    fun encode(payload: Any): String = strictJsonMapper.writeValueAsString(payload)
}
