package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import tools.jackson.databind.ObjectMapper

class TelegramWidgetQueryResponderTest {
    private val json = ObjectMapper()
    private val gateway = mock<TelegramWidgetGateway>()
    private val responder = TelegramWidgetQueryResponder(json, gateway)

    @Test
    fun smallStateIsReturnedImmediately() {
        whenever(gateway.answerQuery(eq("q"), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            responder.reply("q", "OK", "Оновлено", """{"page":2}"""),
        )

        val captured = argumentCaptor<String>()
        verify(gateway).answerQuery(eq("q"), captured.capture())
        assertEquals(2, json.readTree(captured.firstValue).path("state").path("page").asInt())
    }

    @Test
    fun oversizedWrappedStateFallsBackToShortAcknowledgement() {
        whenever(gateway.answerQuery(eq("q"), any())).thenReturn(TelegramDeliveryResult.Delivered)
        val state = json.writeValueAsString(mapOf("name" to "я".repeat(475)))
        assertTrue(state.toByteArray(Charsets.UTF_8).size < 1024)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            responder.reply("q", "OK", "Налаштування оновлено", state),
        )

        val captured = argumentCaptor<String>()
        verify(gateway).answerQuery(eq("q"), captured.capture())
        val answer = json.readTree(captured.firstValue)
        assertEquals("OK", answer.path("status").asText())
        assertFalse(answer.has("state"))
        assertTrue(captured.firstValue.toByteArray(Charsets.UTF_8).size <= 1024)
    }
}
