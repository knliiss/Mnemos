package com.mnemos.social.adapter.out.persistence.spotlight

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.social.application.spotlight.model.ItemDropSnapshot
import com.mnemos.social.application.spotlight.model.NotificationPackSelectionSnapshot
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class DropSpotlightPersistenceAdapter(
    private val repository: DropSpotlightJpaRepository,
    private val objectMapper: ObjectMapper,
) : DropSpotlightRepository {
    override fun save(spotlight: DropSpotlight): DropSpotlight = repository.save(spotlight.toEntity()).toDomain()

    override fun findById(id: DropSpotlightId): DropSpotlight? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun existsByItemDropEventId(itemDropEventId: UUID): Boolean = repository.existsByItemDropEventId(itemDropEventId)

    override fun findLatestByBuyerUserId(userId: UUID): DropSpotlight? =
        repository.findTopByBuyerUserIdOrderByCreatedAtDesc(userId)?.toDomain()

    override fun findDue(now: Instant): List<DropSpotlight> =
        repository
            .findTop20ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(DropSpotlightStatus.QUEUED, now)
            .map { it.toDomain() }

    private fun DropSpotlight.toEntity(): DropSpotlightEntity =
        DropSpotlightEntity(
            id = id.id,
            sequence = sequence,
            buyerUserId = buyerUserId,
            sourceAccountId = sourceAccountId,
            itemDropEventId = itemDrop.id,
            itemId = itemDrop.itemId,
            itemName = itemDrop.itemName,
            itemRarity = itemDrop.itemRarity,
            itemType = itemDrop.itemType,
            droppedFor = itemDrop.droppedFor,
            dropOccurredAt = itemDrop.occurredAt,
            notificationPackId = notificationPack?.personalizationId,
            notificationPackRevision = notificationPack?.revision,
            notificationPackSnapshotJson = notificationPack?.pack?.let(objectMapper::writeValueAsString),
            pricePrime = pricePrime,
            walletRequestId = walletRequestId,
            walletTransactionId = walletTransactionId,
            status = status,
            scheduledAt = scheduledAt,
            publishedAt = publishedAt,
            expiresAt = expiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun DropSpotlightEntity.toDomain(): DropSpotlight =
        DropSpotlight(
            id = DropSpotlightId(id),
            sequence = sequence,
            buyerUserId = buyerUserId,
            sourceAccountId = sourceAccountId,
            itemDrop = ItemDropSnapshot(
                id = itemDropEventId,
                itemId = itemId,
                itemName = itemName,
                itemRarity = itemRarity,
                itemType = itemType,
                droppedFor = droppedFor,
                occurredAt = dropOccurredAt,
            ),
            notificationPack = notificationPackSelection(),
            pricePrime = pricePrime,
            walletRequestId = walletRequestId,
            walletTransactionId = walletTransactionId,
            status = status,
            scheduledAt = scheduledAt,
            publishedAt = publishedAt,
            expiresAt = expiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun DropSpotlightEntity.notificationPackSelection(): NotificationPackSelectionSnapshot? {
        val personalizationId = notificationPackId ?: return null
        val revision = notificationPackRevision ?: return null
        val snapshotJson = notificationPackSnapshotJson ?: return null

        return NotificationPackSelectionSnapshot(
            personalizationId = personalizationId,
            revision = revision,
            pack = objectMapper.readValue(snapshotJson, NotificationPackSnapshotV1::class.java),
        )
    }
}
