package com.mnemos.notification.application.notification.model

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NotificationPayloadTest {
    @Test
    fun `secret drops are limited to swords and pets`() {
        listOf(
            NotificationItemType.AURA,
            NotificationItemType.BOOK,
            NotificationItemType.JEWELRY,
        ).forEach { itemType ->
            assertThrows(IllegalArgumentException::class.java) {
                ItemDropNotificationPayload(
                    droppedFor = "Knalis",
                    itemName = "Secret item",
                    itemType = itemType,
                    itemRarity = NotificationItemRarity.SECRET,
                )
            }
        }
    }

    @Test
    fun `secret swords and pets remain valid`() {
        listOf(
            NotificationItemType.SWORD,
            NotificationItemType.PET,
        ).forEach { itemType ->
            ItemDropNotificationPayload(
                droppedFor = "Knalis",
                itemName = "Secret item",
                itemType = itemType,
                itemRarity = NotificationItemRarity.SECRET,
            )
        }
    }
}
