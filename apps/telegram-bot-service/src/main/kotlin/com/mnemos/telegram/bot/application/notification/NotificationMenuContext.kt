package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuFeedback

object NotificationMenuContext {
    private const val PAGE_KEY = "notifications.page"
    private const val MODE_KEY = "notifications.mode"
    private const val FEEDBACK_KEY = "notifications.feedback"
    private const val DROPPED_FOR_KEY = "notifications.itemDrop.player"
    private const val ITEM_NAME_KEY = "notifications.itemDrop.item"
    private const val RAID_LOCATIONS_KEY = "notifications.raid.locations"
    private const val RAID_PAGE_KEY = "notifications.raid.page"

    fun page(context: Map<String, String>): Int =
        context[PAGE_KEY]
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0

    fun mode(context: Map<String, String>): NotificationCreationMode =
        context[MODE_KEY]
            ?.let { value -> enumValueOrNull<NotificationCreationMode>(value) }
            ?: NotificationCreationMode.LIST

    fun feedback(context: Map<String, String>): NotificationMenuFeedback? =
        context[FEEDBACK_KEY]
            ?.let { value -> enumValueOrNull<NotificationMenuFeedback>(value) }

    fun droppedFor(context: Map<String, String>): String? = context[DROPPED_FOR_KEY]

    fun itemName(context: Map<String, String>): String? = context[ITEM_NAME_KEY]

    fun raidLocations(context: Map<String, String>): Set<Int> =
        context[RAID_LOCATIONS_KEY]
            ?.split(',')
            ?.mapNotNull(String::toIntOrNull)
            ?.filter { location -> location in MIN_RAID_LOCATION..MAX_RAID_LOCATION }
            ?.toSet()
            ?: emptySet()

    fun raidPage(context: Map<String, String>): Int =
        context[RAID_PAGE_KEY]
            ?.toIntOrNull()
            ?.coerceIn(0, MAX_RAID_PAGE)
            ?: 0

    fun withPage(
        context: Map<String, String>,
        page: Int,
    ): Map<String, String> {
        val normalizedPage = page.coerceAtLeast(0)

        return buildMap {
            if (normalizedPage > 0) {
                put(PAGE_KEY, normalizedPage.toString())
            }
        }
    }

    fun refresh(context: Map<String, String>): Map<String, String> =
        when (mode(context)) {
            NotificationCreationMode.LIST -> withPage(context, page(context))
            else -> clearFeedback(context)
        }

    fun startCreation(): Map<String, String> =
        mapOf(MODE_KEY to NotificationCreationMode.TYPE_SELECTION.name)

    fun selectMode(mode: NotificationCreationMode): Map<String, String> {
        require(mode != NotificationCreationMode.LIST)

        return mapOf(MODE_KEY to mode.name)
    }

    fun creationSucceeded(): Map<String, String> =
        mapOf(FEEDBACK_KEY to NotificationMenuFeedback.CREATED.name)

    fun withFeedback(
        context: Map<String, String>,
        feedback: NotificationMenuFeedback,
    ): Map<String, String> = context + (FEEDBACK_KEY to feedback.name)

    fun clearFeedback(context: Map<String, String>): Map<String, String> = context - FEEDBACK_KEY

    fun withDroppedFor(
        context: Map<String, String>,
        droppedFor: String,
    ): Map<String, String> = clearFeedback(context) + (DROPPED_FOR_KEY to droppedFor)

    fun withoutDroppedFor(context: Map<String, String>): Map<String, String> =
        clearFeedback(context) - DROPPED_FOR_KEY

    fun withItemName(
        context: Map<String, String>,
        itemName: String,
    ): Map<String, String> = clearFeedback(context) + (ITEM_NAME_KEY to itemName)

    fun withoutItemName(context: Map<String, String>): Map<String, String> =
        clearFeedback(context) - ITEM_NAME_KEY

    fun toggleRaidLocation(
        context: Map<String, String>,
        location: Int,
    ): Map<String, String> {
        require(location in MIN_RAID_LOCATION..MAX_RAID_LOCATION)

        val currentLocations = raidLocations(context)
        val nextLocations =
            if (location in currentLocations) {
                currentLocations - location
            } else {
                currentLocations + location
            }

        return withRaidLocations(clearFeedback(context), nextLocations)
    }

    fun clearRaidLocations(context: Map<String, String>): Map<String, String> =
        clearFeedback(context) - RAID_LOCATIONS_KEY

    fun withRaidPage(
        context: Map<String, String>,
        page: Int,
    ): Map<String, String> {
        val normalizedPage = page.coerceIn(0, MAX_RAID_PAGE)
        val withoutPage = clearFeedback(context) - RAID_PAGE_KEY

        return if (normalizedPage == 0) {
            withoutPage
        } else {
            withoutPage + (RAID_PAGE_KEY to normalizedPage.toString())
        }
    }

    private fun withRaidLocations(
        context: Map<String, String>,
        locations: Set<Int>,
    ): Map<String, String> {
        val withoutLocations = context - RAID_LOCATIONS_KEY

        if (locations.isEmpty()) {
            return withoutLocations
        }

        val encodedLocations = locations.sorted().joinToString(",")

        return withoutLocations + (RAID_LOCATIONS_KEY to encodedLocations)
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { enumValue -> enumValue.name == value }

    private const val MIN_RAID_LOCATION = 1
    private const val MAX_RAID_LOCATION = 28
    private const val MAX_RAID_PAGE = 1
}
