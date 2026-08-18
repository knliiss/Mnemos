package com.mnemos.realtime.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.ItemDroppedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.adapter.inbound.messaging.event.GameEventBroadcastMapper.toBroadcast
import com.mnemos.realtime.application.broadcast.PublishRealtimeBroadcastService
import org.springframework.stereotype.Component

@Component
class ItemDroppedRealtimeHandler(
    private val publisher: PublishRealtimeBroadcastService,
) : KafkaMessageHandler<ItemDroppedV1> {
    override val definition =
        kafkaMessageDefinition<ItemDroppedV1>(
            EventEventContract.TOPIC,
            EventEventType.ITEM_DROPPED,
            EventEventContract.VERSION,
            EventAggregateType.ITEM_DROP_EVENT,
        )

    override val consumer = "realtime-service.item-dropped"

    override fun handle(message: ConsumedMessage<ItemDroppedV1>) {
        publisher.execute(
            message.payload.toBroadcast(
                eventId = message.aggregateId,
                occurredAt = message.occurredAt,
            ),
        )
    }
}
