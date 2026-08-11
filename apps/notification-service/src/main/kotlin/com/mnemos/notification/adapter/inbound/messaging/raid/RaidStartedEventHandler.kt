package com.mnemos.notification.adapter.inbound.messaging.raid

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.RaidStartedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.adapter.inbound.messaging.raid.RaidStartedEventMapper.toCommand
import com.mnemos.notification.application.raid.usecase.process.ProcessRaidEventUseCase
import org.springframework.stereotype.Component

@Component
class RaidStartedEventHandler(
    private val processRaidEventUseCase: ProcessRaidEventUseCase,
) : KafkaMessageHandler<RaidStartedV1> {
    override val definition: KafkaMessageDefinition<RaidStartedV1> =
        kafkaMessageDefinition(
            topic = EventEventContract.TOPIC,
            type = EventEventType.RAID_STARTED,
            version = EventEventContract.VERSION,
            aggregateType = EventAggregateType.RAID_EVENT,
        )

    override val consumer = "notification-service.raid-started"

    override fun handle(message: ConsumedMessage<RaidStartedV1>) {
        processRaidEventUseCase.execute(message.toCommand())
    }
}
