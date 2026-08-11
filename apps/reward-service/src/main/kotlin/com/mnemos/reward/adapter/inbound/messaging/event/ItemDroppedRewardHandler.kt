package com.mnemos.reward.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.ItemDroppedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.reward.application.itemdrop.usecase.ProcessItemDropRewardCommand
import com.mnemos.reward.application.itemdrop.usecase.ProcessItemDropRewardService
import org.springframework.stereotype.Component

@Component
class ItemDroppedRewardHandler(
    private val service: ProcessItemDropRewardService,
) : KafkaMessageHandler<ItemDroppedV1> {
    override val definition =
        kafkaMessageDefinition<ItemDroppedV1>(
            EventEventContract.TOPIC,
            EventEventType.ITEM_DROPPED,
            EventEventContract.VERSION,
            EventAggregateType.ITEM_DROP_EVENT,
        )

    override val consumer = "reward-service.item-dropped"

    override fun handle(message: ConsumedMessage<ItemDroppedV1>) {
        service.process(
            ProcessItemDropRewardCommand(
                eventId = message.aggregateId,
                droppedFor = message.payload.droppedFor,
                rarity = message.payload.itemRarity,
            ),
        )
    }
}
