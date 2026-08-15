package com.mnemos.realtime.adapter.inbound.messaging.social

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.application.broadcast.PublishRealtimeBroadcastService
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.social.contracts.SocialEventContract
import com.mnemos.social.contracts.SocialEventType
import com.mnemos.social.contracts.event.DropSpotlightPublishedV1
import org.springframework.stereotype.Component

@Component
class DropSpotlightPublishedHandler(
    private val service: PublishRealtimeBroadcastService,
) : KafkaMessageHandler<DropSpotlightPublishedV1> {
    override val definition =
        kafkaMessageDefinition<DropSpotlightPublishedV1>(
            SocialEventContract.TOPIC,
            SocialEventType.DROP_SPOTLIGHT_PUBLISHED,
            SocialEventContract.VERSION,
            SocialEventContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != SOCIAL_SERVICE) {
                throw KafkaMessageProtocolException("Drop spotlight must be produced by social-service")
            }
            if (envelope.aggregateId != envelope.payload.spotlightId) {
                throw KafkaMessageProtocolException("Drop spotlight aggregateId must match spotlightId")
            }
            if (envelope.occurredAt != envelope.payload.publishedAt) {
                throw KafkaMessageProtocolException("Drop spotlight occurredAt must match publishedAt")
            }
        }

    override val consumer = "realtime-service.drop-spotlight-published"

    override fun handle(message: ConsumedMessage<DropSpotlightPublishedV1>) {
        val event = message.payload

        service.execute(
            RealtimeBroadcast(
                id = event.spotlightId,
                sequence = event.sequence,
                occurredAt = event.publishedAt,
                expiresAt = event.expiresAt,
                payload = event,
            ),
        )
    }

    private companion object {
        const val SOCIAL_SERVICE = "social-service"
    }
}
