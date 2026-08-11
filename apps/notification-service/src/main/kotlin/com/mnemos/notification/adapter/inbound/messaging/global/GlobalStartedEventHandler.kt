package com.mnemos.notification.adapter.inbound.messaging.global

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.GlobalStartedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.adapter.inbound.messaging.global.GlobalStartedEventMapper.toCommand
import com.mnemos.notification.application.global.usecase.process.ProcessGlobalEventUseCase
import org.springframework.stereotype.Component

@Component
class GlobalStartedEventHandler(
    private val processGlobalEventUseCase: ProcessGlobalEventUseCase,
) : KafkaMessageHandler<GlobalStartedV1> {
    override val definition: KafkaMessageDefinition<GlobalStartedV1> =
        kafkaMessageDefinition(
            topic = EventEventContract.TOPIC,
            type = EventEventType.GLOBAL_STARTED,
            version = EventEventContract.VERSION,
            aggregateType = EventAggregateType.GLOBAL_EVENT,
        )

    override val consumer = "notification-service.global-started"

    override fun handle(message: ConsumedMessage<GlobalStartedV1>) {
        processGlobalEventUseCase.execute(message.toCommand())
    }
}
