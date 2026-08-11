package com.mnemos.notification.application.notification.model

import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType

sealed interface NotificationPayload {
    val type: NotificationType
}

data class ItemDropNotificationPayload(
    val droppedFor: String,
    val itemName: String,
    val itemType: NotificationItemType,
    val itemRarity: NotificationItemRarity,
) : NotificationPayload {
    override val type = NotificationType.ITEM_DROP
}

data class BoosterNotificationPayload(
    val boosterType: DomainBoosterType,
    val activatedBy: String,
) : NotificationPayload {
    override val type = NotificationType.BOOSTER
}

data class DungeonNotificationPayload(
    val dungeonType: DomainDungeonType,
) : NotificationPayload {
    override val type = NotificationType.DUNGEON
}

data class RaidNotificationPayload(
    val locations: Set<Int>,
) : NotificationPayload {
    override val type = NotificationType.RAID
}

data class GlobalNotificationPayload(
    val eventType: DomainGlobalEventType,
) : NotificationPayload {
    override val type = NotificationType.GLOBAL
}

enum class NotificationItemType {
    SWORD,
    AURA,
    PET,
    BOOK,
    JEWELRY,
}

enum class NotificationItemRarity {
    MYTHICAL,
    SECRET,
}
