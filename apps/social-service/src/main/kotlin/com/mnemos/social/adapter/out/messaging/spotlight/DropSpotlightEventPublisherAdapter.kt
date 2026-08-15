package com.mnemos.social.adapter.out.messaging.spotlight

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.social.application.spotlight.port.out.DropSpotlightEventPublisher
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.contracts.SocialEventType
import com.mnemos.social.contracts.event.DropSpotlightPublishedV1
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import org.springframework.stereotype.Component

@Component
class DropSpotlightEventPublisherAdapter(
    private val publisher: IntegrationEventPublisher,
    private val properties: DropSpotlightProperties,
) : DropSpotlightEventPublisher {
    override fun publish(spotlight: DropSpotlight) {
        val publishedAt = requireNotNull(spotlight.publishedAt)
        val expiresAt = requireNotNull(spotlight.expiresAt)

        publisher.publish(
            event = IntegrationEvent(
                type = SocialEventType.DROP_SPOTLIGHT_PUBLISHED,
                aggregateId = spotlight.id.id,
                occurredAt = publishedAt,
                payload = DropSpotlightPublishedV1(
                    spotlightId = spotlight.id.id,
                    sequence = spotlight.sequence,
                    sourceAccountId = spotlight.sourceAccountId,
                    itemDropEventId = spotlight.itemDrop.id,
                    droppedFor = spotlight.itemDrop.droppedFor,
                    itemId = spotlight.itemDrop.itemId,
                    itemName = spotlight.itemDrop.itemName,
                    itemRarity = spotlight.itemDrop.itemRarity,
                    itemType = spotlight.itemDrop.itemType,
                    notificationPack = spotlight.notificationPack?.pack,
                    publishedAt = publishedAt,
                    expiresAt = expiresAt,
                    minimumPresentationGapSeconds = properties.recipientGap.seconds,
                ),
            ),
            metadata = EventMetadata(actorUserId = spotlight.buyerUserId),
        )
    }
}
