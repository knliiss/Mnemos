package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class NotificationRaidLocationsTest {
    @Test
    fun `missing raid is disabled and empty backend locations mean all locations`() {
        assertEquals(emptySet<Int>(), NotificationRaidLocations.active(null))
        assertEquals(NotificationRaidLocations.all, NotificationRaidLocations.active(raidSubscription(emptySet())))
        assertEquals(setOf(3, 12), NotificationRaidLocations.active(raidSubscription(setOf(3, 12))))
    }

    @Test
    fun `all active locations use canonical empty backend representation`() {
        assertEquals(emptySet<Int>(), NotificationRaidLocations.toBackendLocations(NotificationRaidLocations.all))
        assertEquals(setOf(3, 12), NotificationRaidLocations.toBackendLocations(setOf(3, 12)))
    }

    @Test
    fun `location target mutation is replay safe`() {
        val selected = NotificationRaidLocations.withTarget(emptySet(), location = 3, selected = true)
        val selectedAgain = NotificationRaidLocations.withTarget(selected, location = 3, selected = true)
        val cleared = NotificationRaidLocations.withTarget(selectedAgain, location = 3, selected = false)
        val clearedAgain = NotificationRaidLocations.withTarget(cleared, location = 3, selected = false)

        assertEquals(setOf(3), selected)
        assertEquals(setOf(3), selectedAgain)
        assertEquals(emptySet<Int>(), cleared)
        assertEquals(emptySet<Int>(), clearedAgain)
    }

    private fun raidSubscription(locations: Set<Int>): NotificationSubscription =
        NotificationSubscription(
            id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"),
            type = NotificationSubscriptionType.RAID,
            createdAt = Instant.parse("2026-08-30T12:24:00Z"),
            droppedFor = null,
            itemName = null,
            boosterType = null,
            dungeonType = null,
            globalType = null,
            raidLocations = locations,
        )
}
