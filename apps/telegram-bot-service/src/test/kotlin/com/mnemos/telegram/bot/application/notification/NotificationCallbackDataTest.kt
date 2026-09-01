package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class NotificationCallbackDataTest {
    @Test
    fun `delete request and confirmation callbacks round trip subscription id`() {
        val subscriptionId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")

        val requestCallback = NotificationCallbackData.delete(subscriptionId)
        val confirmCallback = NotificationCallbackData.confirmDelete(subscriptionId)

        assertEquals(subscriptionId, NotificationCallbackData.deletedSubscriptionId(requestCallback))
        assertEquals(subscriptionId, NotificationCallbackData.confirmedDeleteSubscriptionId(confirmCallback))
        assertNull(NotificationCallbackData.deletedSubscriptionId(confirmCallback))
        assertNull(NotificationCallbackData.confirmedDeleteSubscriptionId(requestCallback))
        assertTrue(requestCallback.toByteArray(Charsets.UTF_8).size <= 64)
        assertTrue(confirmCallback.toByteArray(Charsets.UTF_8).size <= 64)
    }

    @Test
    fun `page callback normalizes negative page`() {
        val callbackData = NotificationCallbackData.page(-4)

        assertEquals(0, NotificationCallbackData.selectedPage(callbackData))
    }

    @Test
    fun `malformed and legacy toggle callbacks are ignored`() {
        assertNull(NotificationCallbackData.selectedPage("ntf:p:invalid"))
        assertNull(NotificationCallbackData.deletedSubscriptionId("ntf:d:invalid"))
        assertNull(NotificationCallbackData.confirmedDeleteSubscriptionId("ntf:dc:invalid"))
        assertNull(NotificationCallbackData.selectedBoosterTarget("ntf:b:luck"))
        assertNull(NotificationCallbackData.selectedRaidLocationTarget("ntf:rl:3"))
    }

    @Test
    fun `target state callbacks round trip and stay within Telegram callback limit`() {
        val boosterCallback = NotificationCallbackData.booster(NotificationBoosterType.LUCK, enabled = false)
        val dungeonCallback = NotificationCallbackData.dungeon(NotificationDungeonType.EXPERT, enabled = true)
        val globalCallback = NotificationCallbackData.global(NotificationGlobalType.COMET_CHAOS, enabled = false)
        val raidLocationCallback = NotificationCallbackData.raidLocation(location = 28, selected = true)
        val raidPageCallback = NotificationCallbackData.raidPage(1)
        val callbacks =
            listOf(
                NotificationCallbackData.RAID_EDIT,
                boosterCallback,
                dungeonCallback,
                globalCallback,
                raidLocationCallback,
                raidPageCallback,
            )

        assertEquals(
            NotificationToggleTarget(NotificationBoosterType.LUCK, enabled = false),
            NotificationCallbackData.selectedBoosterTarget(boosterCallback),
        )
        assertEquals(
            NotificationToggleTarget(NotificationDungeonType.EXPERT, enabled = true),
            NotificationCallbackData.selectedDungeonTarget(dungeonCallback),
        )
        assertEquals(
            NotificationToggleTarget(NotificationGlobalType.COMET_CHAOS, enabled = false),
            NotificationCallbackData.selectedGlobalTarget(globalCallback),
        )
        assertEquals(
            NotificationRaidLocationTarget(location = 28, selected = true),
            NotificationCallbackData.selectedRaidLocationTarget(raidLocationCallback),
        )
        assertEquals(1, NotificationCallbackData.selectedRaidPage(raidPageCallback))
        assertTrue(callbacks.all { callback -> callback.toByteArray(Charsets.UTF_8).size <= 64 })
    }
}
