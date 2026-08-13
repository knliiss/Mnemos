package com.mnemos.gateway.moderation

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.moderation.contracts.ModerationEventContract
import com.mnemos.moderation.contracts.ModerationEventType
import com.mnemos.moderation.contracts.event.SanctionStateV1
import com.mnemos.moderation.contracts.shared.SanctionType
import org.springframework.stereotype.Component

@Component
class ModerationDeactivatedHandler(
    private val cache: BanCache,
) : KafkaMessageHandler<SanctionStateV1> {
    override val definition: KafkaMessageDefinition<SanctionStateV1> =
        kafkaMessageDefinition(
            topic = ModerationEventContract.TOPIC,
            type = ModerationEventType.SANCTION_DEACTIVATED,
            version = ModerationEventContract.VERSION,
            aggregateType = ModerationEventContract.AGGREGATE_TYPE,
        )

    override val consumer = "gateway-service.moderation-deactivated"

    override fun handle(message: ConsumedMessage<SanctionStateV1>) {
        val sanction = message.payload

        if (sanction.type != SanctionType.BAN) {
            return
        }

        cache.deactivate(
            sanctionId = sanction.sanctionId,
            userId = sanction.userId,
            occurredAt = sanction.occurredAt,
        )
    }
}
