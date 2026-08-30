package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class NotificationCallbackDataTest {
    @Test
    fun `delete callback round trips subscription id`() {
        val subscriptionId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")

        val callbackData = NotificationCallbackData.delete(subscriptionId)

        assertEquals(subscriptionId, NotificationCallbackData.deletedSubscriptionId(callbackData))
    }

    @Test
    fun `page callback normalizes negative page`() {
        val callbackData = NotificationCallbackData.page(-4)

        assertEquals(0, NotificationCallbackData.selectedPage(callbackData))
    }

    @Test
    fun `malformed callbacks are ignored`() {
        assertNull(NotificationCallbackData.selectedPage("ntf:p:invalid"))
        assertNull(NotificationCallbackData.deletedSubscriptionId("ntf:d:invalid"))
    }

    @Test
    fun `creation callbacks round trip and stay within Telegram callback limit`() {
        val typeCallback = NotificationCallbackData.type(NotificationSubscriptionType.ITEM_DROP)
        val boosterCallback = NotificationCallbackData.booster(NotificationBoosterType.LUCK)
        val dungeonCallback = NotificationCallbackData.dungeon(NotificationDungeonType.EXPERT)
        val globalCallback = NotificationCallbackData.global(NotificationGlobalType.COMET_CHAOS)
        val raidLocationCallback = NotificationCallbackData.raidLocation(28)
        val raidPageCallback = NotificationCallbackData.raidPage(1)
        val callbacks =
            listOf(
                typeCallback,
                boosterCallback,
                dungeonCallback,
                globalCallback,
                raidLocationCallback,
                raidPageCallback,
            )

        assertEquals(NotificationCreationMode.ITEM_DROP, NotificationCallbackData.selectedCreationMode(typeCallback))
        assertEquals(NotificationBoosterType.LUCK, NotificationCallbackData.selectedBoosterType(boosterCallback))
        assertEquals(NotificationDungeonType.EXPERT, NotificationCallbackData.selectedDungeonType(dungeonCallback))
        assertEquals(NotificationGlobalType.COMET_CHAOS, NotificationCallbackData.selectedGlobalType(globalCallback))
        assertEquals(28, NotificationCallbackData.selectedRaidLocation(raidLocationCallback))
        assertEquals(1, NotificationCallbackData.selectedRaidPage(raidPageCallback))
        assertTrue(callbacks.all { callback -> callback.toByteArray(Charsets.UTF_8).size <= 64 })
    }
}
