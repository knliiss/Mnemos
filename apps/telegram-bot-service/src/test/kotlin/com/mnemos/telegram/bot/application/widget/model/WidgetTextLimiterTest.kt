package com.mnemos.telegram.bot.application.widget.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WidgetTextLimiterTest {
    @Test
    fun cutsCyrillicTextWithoutExceedingByteLimit() {
        val text = "Український підпис до предмета"

        val result = WidgetTextLimiter.truncateUtf8(text, 17)

        assertTrue(result.toByteArray(Charsets.UTF_8).size <= 17)
        assertTrue(text.startsWith(result))
    }

    @Test
    fun keepsEmojiCodePointsIntact() {
        val input = "🚀".repeat(20)

        assertEquals("🚀🚀🚀", WidgetTextLimiter.truncateUtf8(input, 12))
        assertEquals("", WidgetTextLimiter.truncateUtf8(input, 3))
    }

    @Test
    fun preservesShortStringsAndHandlesZeroLength() {
        assertEquals("Kirito", WidgetTextLimiter.truncateUtf8("Kirito", 20))
        assertEquals("", WidgetTextLimiter.truncateUtf8("Kirito", 0))
    }
}
