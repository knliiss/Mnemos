package com.mnemos.personalization.contracts.shared

enum class NotificationPackValueKey(
    val placeholder: String,
    val allowedValues: Set<String>,
) {
    ITEM_RARITY(
        placeholder = "rarity",
        allowedValues = setOf("MYTHICAL", "SECRET"),
    ),
    ITEM_TYPE(
        placeholder = "itemType",
        allowedValues = setOf("SWORD", "AURA", "PET", "BOOK", "JEWELRY"),
    ),
    BOOSTER(
        placeholder = "booster",
        allowedValues = setOf("LUCK", "MONEY", "DAMAGE", "POWER"),
    ),
    DUNGEON(
        placeholder = "dungeon",
        allowedValues = setOf("EASY", "MEDIUM", "HARD", "EXPERT"),
    ),
    GLOBAL_EVENT(
        placeholder = "event",
        allowedValues = setOf("DARKNESS", "MOON", "ECLIPSE", "EXPLOSION"),
    ),
    ;

    companion object {
        fun forPlaceholder(placeholder: String): NotificationPackValueKey? =
            entries.firstOrNull { key -> key.placeholder == placeholder }
    }
}
