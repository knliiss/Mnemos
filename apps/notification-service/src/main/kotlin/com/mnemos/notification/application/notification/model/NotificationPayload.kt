package com.mnemos.notification.application.notification.model

import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import java.time.Instant
import java.util.UUID

sealed interface NotificationPayload {
    val type: NotificationType
}

data class ItemDropNotificationPayload(
    val droppedFor: String,
    val itemKey: String? = null,
    val itemName: String,
    val itemType: NotificationItemType,
    val itemRarity: NotificationItemRarity,
) : NotificationPayload {
    init {
        require(itemRarity != NotificationItemRarity.SECRET || itemType in SECRET_ITEM_TYPES) {
            "Secret item drops are only supported for swords and pets"
        }
    }

    override val type = NotificationType.ITEM_DROP

    private companion object {
        val SECRET_ITEM_TYPES = setOf(NotificationItemType.SWORD, NotificationItemType.PET)
    }
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

data class PingNotificationPayload(
    val player: String,
    val message: String,
    val text: String,
    val target: String,
) : NotificationPayload {
    override val type = NotificationType.PING
}

data class PlayerOfflineNotificationPayload(
    val playerId: UUID,
    val player: String,
    val previousRealm: String?,
    val lastSeenOnline: Instant?,
) : NotificationPayload {
    override val type = NotificationType.PLAYER_OFFLINE
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
