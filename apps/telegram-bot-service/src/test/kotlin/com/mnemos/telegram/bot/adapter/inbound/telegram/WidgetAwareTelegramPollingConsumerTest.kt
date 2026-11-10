package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryRouter
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import tools.jackson.databind.ObjectMapper

class WidgetAwareTelegramPollingConsumerTest {
    private val legacy = mock<TelegramLongPollingConsumer>()
    private val widgetRouter = mock<TelegramWidgetQueryRouter>()
    private val consumer =
        WidgetAwareTelegramPollingConsumer(
            telegram = TelegramBotProperties(enabled = true, token = "123:test", miniAppUrl = "https://example.com"),
            widgets = TelegramWidgetProperties(enabled = true),
            legacy = legacy,
            queries = widgetRouter,
        )
    private val json = ObjectMapper()

    @Test
    fun routesWidgetQueryWithoutSendingItToOldSdk() {
        val update =
            json.readTree(
                """{"update_id":21,"widget_query":{"id":"q","from":{"id":11},"chat":{"id":11,"type":"private"},"message_id":8,"type":{"type":"data"},"data":"w1:refresh"}}""",
            )
        whenever(widgetRouter.route(any<TelegramWidgetQueryContext>())).thenReturn(TelegramDeliveryResult.Delivered)

        assertTrue(consumer.process(update, 21))
        verify(widgetRouter).route(any<TelegramWidgetQueryContext>())
        verifyNoInteractions(legacy)
    }

    @Test
    fun retryableWidgetResultDoesNotAcknowledgeUpdate() {
        val update =
            json.readTree(
                """{"update_id":22,"widget_query":{"id":"q","from":{"id":11},"chat":{"id":11,"type":"private"},"message_id":8,"type":{"type":"data"},"data":"w1:refresh"}}""",
            )
        whenever(widgetRouter.route(any<TelegramWidgetQueryContext>())).thenReturn(TelegramDeliveryResult.Retryable)

        assertFalse(consumer.process(update, 22))
    }
}
