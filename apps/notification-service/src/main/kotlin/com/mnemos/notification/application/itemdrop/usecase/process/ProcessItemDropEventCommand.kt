package com.mnemos.notification.application.itemdrop.usecase.process

import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import java.time.Instant
import java.util.UUID

data class ProcessItemDropEventCommand(
    val messageId: UUID,
    val occurredAt: Instant,
    val droppedFor: String,
    val itemKey: String? = null,
    val itemName: String,
    val itemRarity: NotificationItemRarity,
    val itemType: NotificationItemType,
)
