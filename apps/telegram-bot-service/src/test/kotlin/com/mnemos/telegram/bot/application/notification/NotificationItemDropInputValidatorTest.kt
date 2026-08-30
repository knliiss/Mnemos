package com.mnemos.telegram.bot.application.notification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class NotificationItemDropInputValidatorTest {
    @Test
    fun `player input is trimmed and accepts unicode letters digits and underscore`() {
        assertEquals("Кнализ_21", NotificationItemDropInputValidator.player("  Кнализ_21  "))
    }

    @Test
    fun `player input rejects spaces and short names`() {
        assertNull(NotificationItemDropInputValidator.player("abc"))
        assertNull(NotificationItemDropInputValidator.player("bad name"))
    }

    @Test
    fun `item input is trimmed and bounded`() {
        assertEquals("Sword Art", NotificationItemDropInputValidator.item("  Sword Art  "))
        assertNull(NotificationItemDropInputValidator.item("   "))
        assertNull(NotificationItemDropInputValidator.item("x".repeat(25)))
    }
}
