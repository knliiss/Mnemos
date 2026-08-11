package com.mnemos.notification.adapter.inbound.messaging.booster

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.BoosterActivatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.adapter.inbound.messaging.booster.BoosterActivatedEventMapper.toCommand
import com.mnemos.notification.application.booster.usecase.process.ProcessBoosterEventUseCase
import org.springframework.stereotype.Component

@Component
class BoosterActivatedEventHandler(
    private val processBoosterEventUseCase: ProcessBoosterEventUseCase,
) : KafkaMessageHandler<BoosterActivatedV1> {
    override val definition: KafkaMessageDefinition<BoosterActivatedV1> =
        kafkaMessageDefinition(
            topic = EventEventContract.TOPIC,
            type = EventEventType.BOOSTER_ACTIVATED,
            version = EventEventContract.VERSION,
            aggregateType = EventAggregateType.BOOSTER_EVENT,
        )

    override val consumer = "notification-service.booster-activated"

    override fun handle(message: ConsumedMessage<BoosterActivatedV1>) {
        processBoosterEventUseCase.execute(message.toCommand())
    }
}
