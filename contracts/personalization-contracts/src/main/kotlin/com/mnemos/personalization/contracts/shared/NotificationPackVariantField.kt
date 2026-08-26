package com.mnemos.personalization.contracts.shared

enum class NotificationPackVariantField(
    val templateType: NotificationPackTemplateType,
    val valueKey: NotificationPackValueKey,
) {
    ITEM_RARITY(
        templateType = NotificationPackTemplateType.ITEM_DROP,
        valueKey = NotificationPackValueKey.ITEM_RARITY,
    ),
    ITEM_TYPE(
        templateType = NotificationPackTemplateType.ITEM_DROP,
        valueKey = NotificationPackValueKey.ITEM_TYPE,
    ),
    BOOSTER(
        templateType = NotificationPackTemplateType.BOOSTER,
        valueKey = NotificationPackValueKey.BOOSTER,
    ),
    DUNGEON(
        templateType = NotificationPackTemplateType.DUNGEON,
        valueKey = NotificationPackValueKey.DUNGEON,
    ),
    GLOBAL_EVENT(
        templateType = NotificationPackTemplateType.GLOBAL,
        valueKey = NotificationPackValueKey.GLOBAL_EVENT,
    ),
    ;

    val allowedValues: Set<String>
        get() = valueKey.allowedValues
}
