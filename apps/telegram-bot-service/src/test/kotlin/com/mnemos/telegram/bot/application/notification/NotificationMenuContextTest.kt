package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuFeedback
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class NotificationMenuContextTest {
    @Test
    fun `creation mode resets list state and preserves item drop draft`() {
        val creationContext = NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP)
        val withPlayer = NotificationMenuContext.withDroppedFor(creationContext, "knaliz")
        val withItem = NotificationMenuContext.withItemName(withPlayer, "Sword Art")

        assertEquals(NotificationCreationMode.ITEM_DROP, NotificationMenuContext.mode(withItem))
        assertEquals("knaliz", NotificationMenuContext.droppedFor(withItem))
        assertEquals("Sword Art", NotificationMenuContext.itemName(withItem))
        assertEquals(0, NotificationMenuContext.page(withItem))
    }

    @Test
    fun `drop list pagination preserves drop list mode`() {
        val dropsContext = NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP_LIST)
        val secondPage = NotificationMenuContext.withPage(dropsContext, 1)
        val firstPage = NotificationMenuContext.withPage(secondPage, 0)

        assertEquals(NotificationCreationMode.ITEM_DROP_LIST, NotificationMenuContext.mode(secondPage))
        assertEquals(1, NotificationMenuContext.page(secondPage))
        assertEquals(NotificationCreationMode.ITEM_DROP_LIST, NotificationMenuContext.mode(firstPage))
        assertEquals(0, NotificationMenuContext.page(firstPage))
    }

    @Test
    fun `raid location target state is idempotent`() {
        val raidContext = NotificationMenuContext.selectMode(NotificationCreationMode.RAID)
        val selectedOnce = NotificationMenuContext.setRaidLocation(raidContext, location = 12, selected = true)
        val selectedTwice = NotificationMenuContext.setRaidLocation(selectedOnce, location = 12, selected = true)
        val clearedOnce = NotificationMenuContext.setRaidLocation(selectedTwice, location = 12, selected = false)
        val clearedTwice = NotificationMenuContext.setRaidLocation(clearedOnce, location = 12, selected = false)

        assertEquals(setOf(12), NotificationMenuContext.raidLocations(selectedOnce))
        assertEquals(setOf(12), NotificationMenuContext.raidLocations(selectedTwice))
        assertEquals(emptySet<Int>(), NotificationMenuContext.raidLocations(clearedOnce))
        assertEquals(emptySet<Int>(), NotificationMenuContext.raidLocations(clearedTwice))
    }

    @Test
    fun `successful creation returns to dashboard with feedback`() {
        val context = NotificationMenuContext.creationSucceeded()

        assertEquals(NotificationCreationMode.LIST, NotificationMenuContext.mode(context))
        assertEquals(NotificationMenuFeedback.CREATED, NotificationMenuContext.feedback(context))
        assertFalse(context.containsValue(NotificationCreationMode.ITEM_DROP.name))
    }
}
