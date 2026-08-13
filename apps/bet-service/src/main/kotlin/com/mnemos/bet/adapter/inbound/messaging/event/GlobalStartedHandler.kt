package com.mnemos.bet.adapter.inbound.messaging.event

import com.mnemos.bet.application.eventoccurrence.usecase.record.RecordGlobalOccurrenceCommand
import com.mnemos.bet.application.eventoccurrence.usecase.record.RecordGlobalOccurrenceUseCase
import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.occurrence.model.ExternalEventId
import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.GlobalStartedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import org.springframework.stereotype.Component

@Component
class GlobalStartedHandler(
    private val useCase: RecordGlobalOccurrenceUseCase,
) : KafkaMessageHandler<GlobalStartedV1> {
    override val definition =
        kafkaMessageDefinition<GlobalStartedV1>(
            EventEventContract.TOPIC,
            EventEventType.GLOBAL_STARTED,
            EventEventContract.VERSION,
            EventAggregateType.GLOBAL_EVENT,
        )

    override val consumer = "bet-service.global-started"

    override fun handle(message: ConsumedMessage<GlobalStartedV1>) {
        val eventType =
            DomainBetGlobalEventType.entries.firstOrNull { it.name == message.payload.type.name }
                ?: return

        useCase.execute(
            RecordGlobalOccurrenceCommand(
                eventId = ExternalEventId(message.aggregateId),
                eventType = eventType,
                occurredAt = message.occurredAt,
            ),
        )
    }
}
