package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol

import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorMessageEnvelope
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorReportEnvelope
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorStateMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorUpdateReadyMessage
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class CollectorTransportMessageCodec(
    private val jsonMapper: JsonMapper,
) {
    fun readEnvelope(payload: String): CollectorMessageEnvelope =
        jsonMapper.readValue(
            payload,
            CollectorMessageEnvelope::class.java,
        )

    fun readReport(payload: String): CollectorReportEnvelope =
        jsonMapper.readValue(
            payload,
            CollectorReportEnvelope::class.java,
        )

    fun readState(payload: String): CollectorStateMessage =
        jsonMapper.readValue(
            payload,
            CollectorStateMessage::class.java,
        )

    fun readUpdateReady(payload: String): CollectorUpdateReadyMessage =
        jsonMapper.readValue(
            payload,
            CollectorUpdateReadyMessage::class.java,
        )

    fun encode(payload: Any): String = jsonMapper.writeValueAsString(payload)
}
