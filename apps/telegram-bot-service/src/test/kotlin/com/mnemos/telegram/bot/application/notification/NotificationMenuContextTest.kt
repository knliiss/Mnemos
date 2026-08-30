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
    fun `raid locations toggle and encode deterministically`() {
        val raidContext = NotificationMenuContext.selectMode(NotificationCreationMode.RAID)
        val first = NotificationMenuContext.toggleRaidLocation(raidContext, 12)
        val second = NotificationMenuContext.toggleRaidLocation(first, 3)
        val removed = NotificationMenuContext.toggleRaidLocation(second, 12)

        assertEquals(setOf(3, 12), NotificationMenuContext.raidLocations(second))
        assertEquals(setOf(3), NotificationMenuContext.raidLocations(removed))
    }

    @Test
    fun `successful creation returns to list with feedback`() {
        val context = NotificationMenuContext.creationSucceeded()

        assertEquals(NotificationCreationMode.LIST, NotificationMenuContext.mode(context))
        assertEquals(NotificationMenuFeedback.CREATED, NotificationMenuContext.feedback(context))
        assertFalse(context.containsValue(NotificationCreationMode.ITEM_DROP.name))
    }
}
