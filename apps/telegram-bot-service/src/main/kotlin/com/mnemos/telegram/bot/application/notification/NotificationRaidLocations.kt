package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription

object NotificationRaidLocations {
    const val MIN = 1
    const val MAX = 28

    val all: Set<Int> = (MIN..MAX).toSet()

    fun active(subscription: NotificationSubscription?): Set<Int> =
        when {
            subscription == null -> emptySet()
            subscription.raidLocations.isEmpty() -> all
            else -> subscription.raidLocations
        }

    fun withTarget(
        current: Set<Int>,
        location: Int,
        selected: Boolean,
    ): Set<Int> {
        require(location in MIN..MAX)

        return if (selected) {
            current + location
        } else {
            current - location
        }
    }

    fun toBackendLocations(activeLocations: Set<Int>): Set<Int> =
        if (activeLocations == all) {
            emptySet()
        } else {
            activeLocations
        }
}
