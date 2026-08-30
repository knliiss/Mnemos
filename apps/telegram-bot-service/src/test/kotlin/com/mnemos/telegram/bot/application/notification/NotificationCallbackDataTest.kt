package com.mnemos.telegram.bot.application.notification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
}
