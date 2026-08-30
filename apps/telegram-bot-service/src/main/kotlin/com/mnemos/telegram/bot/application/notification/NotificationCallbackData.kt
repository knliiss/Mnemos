package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import java.util.UUID

object NotificationCallbackData {
    const val PREFIX = "ntf:"
    const val REFRESH = "${PREFIX}r"
    const val CREATE = "${PREFIX}c"
    const val CREATE_TYPES = "${PREFIX}ct"
    const val ITEM_DROP_PLAYER = "${PREFIX}ip"
    const val ITEM_DROP_ITEM = "${PREFIX}ii"
    const val ITEM_DROP_CLEAR_PLAYER = "${PREFIX}ipc"
    const val ITEM_DROP_CLEAR_ITEM = "${PREFIX}iic"
    const val ITEM_DROP_CREATE = "${PREFIX}is"
    const val ITEM_DROP_INPUT_CANCEL = "${PREFIX}ix"
    const val RAID_CLEAR = "${PREFIX}ra"
    const val RAID_CREATE = "${PREFIX}rs"

    private const val PAGE_PREFIX = "${PREFIX}p:"
    private const val DELETE_PREFIX = "${PREFIX}d:"
    private const val TYPE_PREFIX = "${PREFIX}t:"
    private const val BOOSTER_PREFIX = "${PREFIX}b:"
    private const val DUNGEON_PREFIX = "${PREFIX}u:"
    private const val GLOBAL_PREFIX = "${PREFIX}g:"
    private const val RAID_LOCATION_PREFIX = "${PREFIX}rl:"
    private const val RAID_PAGE_PREFIX = "${PREFIX}rp:"

    fun page(page: Int): String = "$PAGE_PREFIX${page.coerceAtLeast(0)}"

    fun delete(subscriptionId: UUID): String = "$DELETE_PREFIX$subscriptionId"

    fun type(type: NotificationSubscriptionType): String = "$TYPE_PREFIX${typeCode(type)}"

    fun booster(type: NotificationBoosterType): String = "$BOOSTER_PREFIX${type.name.lowercase()}"

    fun dungeon(type: NotificationDungeonType): String = "$DUNGEON_PREFIX${type.name.lowercase()}"

    fun global(type: NotificationGlobalType): String = "$GLOBAL_PREFIX${type.name.lowercase()}"

    fun raidLocation(location: Int): String = "$RAID_LOCATION_PREFIX$location"

    fun raidPage(page: Int): String = "$RAID_PAGE_PREFIX${page.coerceIn(0, 1)}"

    fun selectedPage(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(PAGE_PREFIX) }
            ?.removePrefix(PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page >= 0 }

    fun deletedSubscriptionId(data: String): UUID? =
        data
            .takeIf { value -> value.startsWith(DELETE_PREFIX) }
            ?.removePrefix(DELETE_PREFIX)
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    fun selectedCreationMode(data: String): NotificationCreationMode? =
        data
            .takeIf { value -> value.startsWith(TYPE_PREFIX) }
            ?.removePrefix(TYPE_PREFIX)
            ?.let(::creationModeForCode)

    fun selectedBoosterType(data: String): NotificationBoosterType? = enumFromData(data, BOOSTER_PREFIX)

    fun selectedDungeonType(data: String): NotificationDungeonType? = enumFromData(data, DUNGEON_PREFIX)

    fun selectedGlobalType(data: String): NotificationGlobalType? = enumFromData(data, GLOBAL_PREFIX)

    fun selectedRaidLocation(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(RAID_LOCATION_PREFIX) }
            ?.removePrefix(RAID_LOCATION_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { location -> location in 1..28 }

    fun selectedRaidPage(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(RAID_PAGE_PREFIX) }
            ?.removePrefix(RAID_PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page in 0..1 }

    private fun typeCode(type: NotificationSubscriptionType): String =
        when (type) {
            NotificationSubscriptionType.ITEM_DROP -> "i"
            NotificationSubscriptionType.BOOSTER -> "b"
            NotificationSubscriptionType.DUNGEON -> "d"
            NotificationSubscriptionType.RAID -> "r"
            NotificationSubscriptionType.GLOBAL -> "g"
        }

    private fun creationModeForCode(code: String): NotificationCreationMode? =
        when (code) {
            "i" -> NotificationCreationMode.ITEM_DROP
            "b" -> NotificationCreationMode.BOOSTER
            "d" -> NotificationCreationMode.DUNGEON
            "r" -> NotificationCreationMode.RAID
            "g" -> NotificationCreationMode.GLOBAL
            else -> null
        }

    private inline fun <reified T : Enum<T>> enumFromData(
        data: String,
        prefix: String,
    ): T? {
        val name =
            data
                .takeIf { value -> value.startsWith(prefix) }
                ?.removePrefix(prefix)
                ?.uppercase()
                ?: return null

        return enumValues<T>().firstOrNull { value -> value.name == name }
    }
}
