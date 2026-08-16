package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import com.mnemos.social.domain.spotlight.model.NotificationPackSelectionSnapshot
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class DropSpotlightPersistenceAdapter(
    private val repository: DropSpotlightJpaRepository,
) : DropSpotlightRepository {
    override fun save(spotlight: DropSpotlight): DropSpotlight = repository.save(spotlight.toEntity()).toDomain()

    override fun findById(id: DropSpotlightId): DropSpotlight? =
        repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdForUpdate(id: DropSpotlightId): DropSpotlight? =
        repository.findByIdForUpdate(id.id)?.toDomain()

    override fun existsNonFailedByItemDropEventId(itemDropEventId: UUID): Boolean =
        repository.existsByItemDropEventIdAndStatusNot(
            itemDropEventId = itemDropEventId,
            status = DropSpotlightStatus.PAYMENT_FAILED,
        )

    override fun findLatestByBuyerUserId(userId: UUID): DropSpotlight? =
        repository.findTopByBuyerUserIdOrderByCreatedAtDesc(userId)?.toDomain()

    override fun findNextDue(now: Instant): DropSpotlight? =
        repository
            .findFirstByStatusAndScheduledAtLessThanEqualOrderBySequenceAsc(
                status = DropSpotlightStatus.QUEUED,
                scheduledAt = now,
            )?.toDomain()

    override fun findLatestPublishedAt(): Instant? =
        repository
            .findTopByStatusAndPublishedAtIsNotNullOrderByPublishedAtDesc(DropSpotlightStatus.PUBLISHED)
            ?.publishedAt

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
            notificationPackSnapshotJson = notificationPack?.snapshotJson,
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
            itemDrop =
                ItemDropSnapshot(
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
            snapshotJson = snapshotJson,
        )
    }
}
