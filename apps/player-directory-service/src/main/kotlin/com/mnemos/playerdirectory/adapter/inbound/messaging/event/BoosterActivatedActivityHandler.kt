package com.mnemos.playerdirectory.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.BoosterActivatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.playerdirectory.application.activity.model.PlayerEventActivityType
import com.mnemos.playerdirectory.application.activity.usecase.observe.ObservePlayerEventActivityUseCase
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import org.springframework.stereotype.Component

@Component
class BoosterActivatedActivityHandler(
    private val observePlayerEventActivityUseCase: ObservePlayerEventActivityUseCase,
) : KafkaMessageHandler<BoosterActivatedV1> {
    override val definition =
        kafkaMessageDefinition<BoosterActivatedV1>(
            EventEventContract.TOPIC,
            EventEventType.BOOSTER_ACTIVATED,
            EventEventContract.VERSION,
            EventAggregateType.BOOSTER_EVENT,
        ) { envelope ->
            if (envelope.producer != EVENT_SERVICE) {
                throw KafkaMessageProtocolException("Booster events must be produced by event-service")
            }
        }

    override val consumer = "player-directory-service.booster-activity"

    override fun handle(message: ConsumedMessage<BoosterActivatedV1>) {
        observePlayerEventActivityUseCase.execute(
            nickname = PlayerNickname.of(message.payload.activatedBy),
            occurredAt = message.occurredAt,
            type = PlayerEventActivityType.BOOSTER,
        )
    }

    private companion object {
        const val EVENT_SERVICE = "event-service"
    }
}
