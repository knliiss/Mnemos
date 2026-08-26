package com.mnemos.playerdirectory.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.ItemDroppedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.playerdirectory.application.activity.usecase.observe.ObserveItemDropActivityUseCase
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import org.springframework.stereotype.Component

@Component
class ItemDroppedActivityHandler(
    private val observeItemDropActivityUseCase: ObserveItemDropActivityUseCase,
) : KafkaMessageHandler<ItemDroppedV1> {
    override val definition =
        kafkaMessageDefinition<ItemDroppedV1>(
            EventEventContract.TOPIC,
            EventEventType.ITEM_DROPPED,
            EventEventContract.VERSION,
            EventAggregateType.ITEM_DROP_EVENT,
        ) { envelope ->
            if (envelope.producer != EVENT_SERVICE) {
                throw KafkaMessageProtocolException("Item drops must be produced by event-service")
            }
        }

    override val consumer = "player-directory-service.item-drop-activity"

    override fun handle(message: ConsumedMessage<ItemDroppedV1>) {
        observeItemDropActivityUseCase.execute(
            nickname = PlayerNickname.of(message.payload.droppedFor),
            occurredAt = message.occurredAt,
        )
    }

    private companion object {
        const val EVENT_SERVICE = "event-service"
    }
}
