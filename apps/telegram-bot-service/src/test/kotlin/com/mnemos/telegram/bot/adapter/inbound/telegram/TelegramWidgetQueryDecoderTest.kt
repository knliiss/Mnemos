package com.mnemos.telegram.bot.adapter.inbound.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

class TelegramWidgetQueryDecoderTest {
    private val mapper = ObjectMapper()

    @Test
    fun readsPrivateWidgetQueryActorAndMessage() {
        val node =
            mapper.readTree(
                """{"id":"widget-query-1","from":{"id":123},"chat":{"id":123,"type":"private"},"message_id":19,"type":{"type":"data"},"data":"w1:refresh"}""",
            )

        val decoded = TelegramWidgetQueryDecoder.parse(node)

        assertEquals("widget-query-1", decoded?.queryId)
        assertEquals(123L, decoded?.actorId)
        assertEquals(19, decoded?.messageId)
        assertEquals("w1:refresh", decoded?.data)
        assertTrue(decoded?.isPrivate == true)
    }

    @Test
    fun refusesIncompleteOrInlineQueries() {
        assertNull(TelegramWidgetQueryDecoder.parse(mapper.readTree("""{"id":"q","data":"x"}""")))
        assertNull(TelegramWidgetQueryDecoder.parse(mapper.readTree("""{"id":"q","inline_message_id":"x"}""")))
    }
}
