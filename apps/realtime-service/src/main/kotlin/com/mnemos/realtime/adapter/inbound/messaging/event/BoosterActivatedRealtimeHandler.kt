package com.mnemos.realtime.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.BoosterActivatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.adapter.inbound.messaging.event.GameEventBroadcastMapper.toBroadcast
import com.mnemos.realtime.application.broadcast.PublishRealtimeBroadcastService
import org.springframework.stereotype.Component

@Component
class BoosterActivatedRealtimeHandler(
    private val publisher: PublishRealtimeBroadcastService,
) : KafkaMessageHandler<BoosterActivatedV1> {
    override val definition =
        kafkaMessageDefinition<BoosterActivatedV1>(
            EventEventContract.TOPIC,
            EventEventType.BOOSTER_ACTIVATED,
            EventEventContract.VERSION,
            EventAggregateType.BOOSTER_EVENT,
        )

    override val consumer = "realtime-service.booster-activated"

    override fun handle(message: ConsumedMessage<BoosterActivatedV1>) {
        publisher.execute(
            message.payload.toBroadcast(
                eventId = message.aggregateId,
                occurredAt = message.occurredAt,
            ),
        )
    }
}
