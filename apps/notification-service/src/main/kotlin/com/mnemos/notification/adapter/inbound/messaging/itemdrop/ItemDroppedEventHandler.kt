package com.mnemos.notification.adapter.inbound.messaging.itemdrop

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.ItemDroppedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.adapter.inbound.messaging.itemdrop.ItemDroppedEventMapper.toCommand
import com.mnemos.notification.application.itemdrop.internal.InternalDropArbitrationService
import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventUseCase
import org.springframework.stereotype.Component

@Component
class ItemDroppedEventHandler(
    private val processItemDropEventUseCase: ProcessItemDropEventUseCase,
    private val internalDropArbitrationService: InternalDropArbitrationService,
) : KafkaMessageHandler<ItemDroppedV1> {
    override val definition: KafkaMessageDefinition<ItemDroppedV1> =
        kafkaMessageDefinition(
            topic = EventEventContract.TOPIC,
            type = EventEventType.ITEM_DROPPED,
            version = EventEventContract.VERSION,
            aggregateType = EventAggregateType.ITEM_DROP_EVENT,
        )

    override val consumer = "notification-service.item-dropped"

    override fun handle(message: ConsumedMessage<ItemDroppedV1>) {
        val command = message.toCommand()

        if (internalDropArbitrationService.shouldDispatchStandard(command)) {
            processItemDropEventUseCase.execute(command)
        }
    }
}
