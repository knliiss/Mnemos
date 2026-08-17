package com.mnemos.realtime.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.GlobalStartedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.adapter.inbound.messaging.event.GameEventBroadcastMapper.toBroadcast
import com.mnemos.realtime.application.broadcast.PublishRealtimeBroadcastService
import org.springframework.stereotype.Component

@Component
class GlobalStartedRealtimeHandler(
    private val publisher: PublishRealtimeBroadcastService,
) : KafkaMessageHandler<GlobalStartedV1> {
    override val definition =
        kafkaMessageDefinition<GlobalStartedV1>(
            EventEventContract.TOPIC,
            EventEventType.GLOBAL_STARTED,
            EventEventContract.VERSION,
            EventAggregateType.GLOBAL_EVENT,
        )

    override val consumer = "realtime-service.global-started"

    override fun handle(message: ConsumedMessage<GlobalStartedV1>) {
        publisher.execute(
            message.payload.toBroadcast(
                eventId = message.aggregateId,
                occurredAt = message.occurredAt,
            ),
        )
    }
}
