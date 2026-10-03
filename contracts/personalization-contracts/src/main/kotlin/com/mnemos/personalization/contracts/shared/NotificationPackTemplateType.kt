package com.mnemos.personalization.contracts.shared

enum class NotificationPackTemplateType(
    val allowedPlaceholders: Set<String>,
) {
    ITEM_DROP(setOf("player", "item", "rarity", "itemType")),
    BOOSTER(setOf("player", "booster")),
    DUNGEON(setOf("dungeon")),
    RAID(setOf("locations")),
    GLOBAL(setOf("event")),
    PING(setOf("player", "message", "target", "text")),
    PLAYER_OFFLINE(setOf("player", "realm", "lastSeen")),
}
