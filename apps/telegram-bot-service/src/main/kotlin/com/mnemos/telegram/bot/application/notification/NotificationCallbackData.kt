package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import java.util.UUID

data class NotificationToggleTarget<T>(
    val value: T,
    val enabled: Boolean,
)

data class NotificationRaidLocationTarget(
    val location: Int,
    val selected: Boolean,
)

object NotificationCallbackData {
    const val PREFIX = "ntf:"
    const val REFRESH = "${PREFIX}r"
    const val CREATE = "${PREFIX}c"
    const val CREATE_TYPES = "${PREFIX}ct"
    const val DROPS = "${PREFIX}dl"
    const val DELETE_CANCEL = "${PREFIX}dx"
    const val ITEM_DROP_PLAYER = "${PREFIX}ip"
    const val ITEM_DROP_ITEM = "${PREFIX}ii"
    const val ITEM_DROP_CLEAR_PLAYER = "${PREFIX}ipc"
    const val ITEM_DROP_CLEAR_ITEM = "${PREFIX}iic"
    const val ITEM_DROP_CREATE = "${PREFIX}is"
    const val ITEM_DROP_INPUT_CANCEL = "${PREFIX}ix"
    const val RAID_CLEAR = "${PREFIX}ra"
    const val RAID_SAVE = "${PREFIX}rs"
    const val RAID_CREATE = RAID_SAVE

    private const val PAGE_PREFIX = "${PREFIX}p:"
    private const val DELETE_PREFIX = "${PREFIX}d:"
    private const val DELETE_CONFIRM_PREFIX = "${PREFIX}dc:"
    private const val TYPE_PREFIX = "${PREFIX}t:"
    private const val BOOSTER_PREFIX = "${PREFIX}b:"
    private const val DUNGEON_PREFIX = "${PREFIX}u:"
    private const val GLOBAL_PREFIX = "${PREFIX}g:"
    private const val RAID_LOCATION_PREFIX = "${PREFIX}rl:"
    private const val RAID_PAGE_PREFIX = "${PREFIX}rp:"

    fun page(page: Int): String = "$PAGE_PREFIX${page.coerceAtLeast(0)}"

    fun delete(subscriptionId: UUID): String = "$DELETE_PREFIX$subscriptionId"

    fun confirmDelete(subscriptionId: UUID): String = "$DELETE_CONFIRM_PREFIX$subscriptionId"

    fun type(type: NotificationSubscriptionType): String = "$TYPE_PREFIX${typeCode(type)}"

    fun booster(
        type: NotificationBoosterType,
        enabled: Boolean,
    ): String = "$BOOSTER_PREFIX${type.name.lowercase()}:${stateCode(enabled)}"

    fun dungeon(
        type: NotificationDungeonType,
        enabled: Boolean,
    ): String = "$DUNGEON_PREFIX${type.name.lowercase()}:${stateCode(enabled)}"

    fun global(
        type: NotificationGlobalType,
        enabled: Boolean,
    ): String = "$GLOBAL_PREFIX${type.name.lowercase()}:${stateCode(enabled)}"

    fun raidLocation(
        location: Int,
        selected: Boolean,
    ): String {
        require(location in MIN_RAID_LOCATION..MAX_RAID_LOCATION)

        return "$RAID_LOCATION_PREFIX$location:${stateCode(selected)}"
    }

    fun raidPage(page: Int): String = "$RAID_PAGE_PREFIX${page.coerceIn(0, 1)}"

    fun selectedPage(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(PAGE_PREFIX) }
            ?.removePrefix(PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page >= 0 }

    fun deletedSubscriptionId(data: String): UUID? =
        uuidFromPrefixedData(
            data = data,
            prefix = DELETE_PREFIX,
        )

    fun confirmedDeleteSubscriptionId(data: String): UUID? =
        uuidFromPrefixedData(
            data = data,
            prefix = DELETE_CONFIRM_PREFIX,
        )

    fun selectedCreationMode(data: String): NotificationCreationMode? =
        data
            .takeIf { value -> value.startsWith(TYPE_PREFIX) }
            ?.removePrefix(TYPE_PREFIX)
            ?.let(::creationModeForCode)

    fun selectedBoosterTarget(data: String): NotificationToggleTarget<NotificationBoosterType>? = enumTargetFromData(data, BOOSTER_PREFIX)

    fun selectedDungeonTarget(data: String): NotificationToggleTarget<NotificationDungeonType>? = enumTargetFromData(data, DUNGEON_PREFIX)

    fun selectedGlobalTarget(data: String): NotificationToggleTarget<NotificationGlobalType>? = enumTargetFromData(data, GLOBAL_PREFIX)

    fun selectedRaidLocationTarget(data: String): NotificationRaidLocationTarget? {
        val payload =
            data
                .takeIf { value -> value.startsWith(RAID_LOCATION_PREFIX) }
                ?.removePrefix(RAID_LOCATION_PREFIX)
                ?: return null
        val target = splitTarget(payload) ?: return null
        val location = target.first.toIntOrNull() ?: return null

        if (location !in MIN_RAID_LOCATION..MAX_RAID_LOCATION) {
            return null
        }

        return NotificationRaidLocationTarget(
            location = location,
            selected = target.second,
        )
    }

    fun selectedRaidPage(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(RAID_PAGE_PREFIX) }
            ?.removePrefix(RAID_PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page in 0..1 }

    private fun uuidFromPrefixedData(
        data: String,
        prefix: String,
    ): UUID? =
        data
            .takeIf { value -> value.startsWith(prefix) }
            ?.removePrefix(prefix)
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

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

    private inline fun <reified T : Enum<T>> enumTargetFromData(
        data: String,
        prefix: String,
    ): NotificationToggleTarget<T>? {
        val payload =
            data
                .takeIf { value -> value.startsWith(prefix) }
                ?.removePrefix(prefix)
                ?: return null
        val target = splitTarget(payload) ?: return null
        val enumName = target.first.uppercase()
        val enumValue = enumValues<T>().firstOrNull { value -> value.name == enumName } ?: return null

        return NotificationToggleTarget(
            value = enumValue,
            enabled = target.second,
        )
    }

    private fun splitTarget(value: String): Pair<String, Boolean>? {
        val separatorIndex = value.lastIndexOf(':')

        if (separatorIndex <= 0 || separatorIndex == value.lastIndex) {
            return null
        }

        val enabled = parseState(value.substring(separatorIndex + 1)) ?: return null

        return value.substring(0, separatorIndex) to enabled
    }

    private fun stateCode(enabled: Boolean): String = if (enabled) ENABLED_STATE else DISABLED_STATE

    private fun parseState(value: String): Boolean? =
        when (value) {
            ENABLED_STATE -> true
            DISABLED_STATE -> false
            else -> null
        }

    private const val ENABLED_STATE = "1"
    private const val DISABLED_STATE = "0"
    private const val MIN_RAID_LOCATION = 1
    private const val MAX_RAID_LOCATION = 28
}
