package com.mnemos.notification.adapter.out.persistence.itemdrop

import com.mnemos.notification.domain.itemdrop.model.ItemDropSubscription

object ItemDropSubscriptionPersistenceMapper {
    fun ItemDropSubscription.toEntity(): ItemDropSubscriptionEntity =
        ItemDropSubscriptionEntity(
            subscriptionId = id.id,
            droppedFor = droppedFor,
            itemName = itemName,
        )

    fun ItemDropSubscription.toEquivalenceKey(): String {
        val normalizedDroppedFor = droppedFor?.lowercase()
        val normalizedItemName = itemName?.lowercase()

        return "${normalizedDroppedFor.toKeyPart()}|${normalizedItemName.toKeyPart()}"
    }

    private fun String?.toKeyPart(): String =
        if (this == null) {
            "-1:"
        } else {
            "$length:$this"
        }
}
