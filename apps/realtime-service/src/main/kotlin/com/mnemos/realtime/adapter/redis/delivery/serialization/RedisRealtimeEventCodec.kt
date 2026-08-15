package com.mnemos.realtime.adapter.redis.delivery.serialization

import com.mnemos.realtime.application.delivery.model.BetOperationCompletedPayload
import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.model.RealtimeEventPayload
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.application.delivery.model.WalletTransactionFailedPayload
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Component
class RedisRealtimeEventCodec(
    private val jsonMapper: JsonMapper,
) {
    fun encode(event: RealtimeEvent): String =
        jsonMapper.writeValueAsString(
            RedisRealtimeEventDocument(
                id = event.id,
                userId = event.userId,
                type = event.type.eventName,
                occurredAt = event.occurredAt,
                operationId = event.operationId,
                payload = jsonMapper.valueToTree(event.payload),
            ),
        )

    fun decode(serialized: String): RealtimeEvent {
        val document =
            jsonMapper.readValue(
                serialized,
                RedisRealtimeEventDocument::class.java,
            )
        val type = RealtimeEventType.fromEventName(document.type)

        return RealtimeEvent(
            id = document.id,
            userId = document.userId,
            type = type,
            occurredAt = document.occurredAt,
            operationId = document.operationId,
            payload = decodePayload(type, document.payload),
        )
    }

    private fun decodePayload(
        type: RealtimeEventType,
        payload: JsonNode,
    ): RealtimeEventPayload =
        when (type) {
            RealtimeEventType.WALLET_TRANSACTION_COMPLETED ->
                jsonMapper.treeToValue(
                    payload,
                    WalletTransactionCompletedPayload::class.java,
                )

            RealtimeEventType.WALLET_TRANSACTION_FAILED ->
                jsonMapper.treeToValue(
                    payload,
                    WalletTransactionFailedPayload::class.java,
                )

            RealtimeEventType.BET_OPERATION_COMPLETED ->
                jsonMapper.treeToValue(
                    payload,
                    BetOperationCompletedPayload::class.java,
                )
        }

    private data class RedisRealtimeEventDocument(
        val id: UUID,
        val userId: UUID,
        val type: String,
        val occurredAt: Instant,
        val operationId: UUID?,
        val payload: JsonNode,
    )
}
