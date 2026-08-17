package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import com.mnemos.social.domain.spotlight.model.NotificationPackSelectionSnapshot

object DropSpotlightPersistenceMapper {
    fun DropSpotlight.toEntity(): DropSpotlightEntity =
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

    fun DropSpotlightEntity.toDomain(): DropSpotlight =
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
