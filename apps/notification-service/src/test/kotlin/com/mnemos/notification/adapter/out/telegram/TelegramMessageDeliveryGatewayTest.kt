package com.mnemos.notification.adapter.out.telegram

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.MessageParseMode
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.configuration.telegram.TelegramBotProperties
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TelegramMessageDeliveryGatewayTest {
    private val bot = mock<TelegramBot>()

    private val gateway =
        TelegramMessageDeliveryGateway(
            bot = bot,
            properties =
                TelegramBotProperties(
                    enabled = true,
                    token = "token",
                    miniAppUrl = "https://app.example.com",
                ),
        )

    @Test
    fun `successful telegram response is delivered`() {
        val response = response(ok = true)

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                TelegramId(123L),
                RenderedMessage("hello"),
            )

        assertEquals(
            DeliveryResult.Delivered,
            result,
        )
    }

    @Test
    fun `telegram 429 is retryable`() {
        val response =
            response(
                ok = false,
                errorCode = 429,
                description = "Too Many Requests",
            )

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                TelegramId(123L),
                RenderedMessage("hello"),
            )

        assertInstanceOf(
            DeliveryResult.Retryable::class.java,
            result,
        )
    }

    @Test
    fun `telegram server error is retryable`() {
        val response =
            response(
                ok = false,
                errorCode = 503,
                description = "Service Unavailable",
            )

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                TelegramId(123L),
                RenderedMessage("hello"),
            )

        assertInstanceOf(
            DeliveryResult.Retryable::class.java,
            result,
        )
    }

    @Test
    fun `telegram client error is permanent failure`() {
        val response =
            response(
                ok = false,
                errorCode = 403,
                description = "bot was blocked by the user",
            )

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                TelegramId(123L),
                RenderedMessage("hello"),
            )

        assertEquals(
            DeliveryResult.PermanentFailure(
                "bot was blocked by the user",
            ),
            result,
        )
    }

    @Test
    fun `telegram exception is retryable`() {
        whenever(bot.execute(any<SendMessage>()))
            .thenThrow(IllegalStateException("network"))

        val result =
            gateway.deliver(
                TelegramId(123L),
                RenderedMessage("hello"),
            )

        assertInstanceOf(
            DeliveryResult.Retryable::class.java,
            result,
        )
    }

    @Test
    fun `all supported parse modes can be sent`() {
        val response = response(ok = true)

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        MessageParseMode.entries.forEach { parseMode ->
            val result =
                gateway.deliver(
                    TelegramId(123L),
                    RenderedMessage(
                        text = "hello",
                        parseMode = parseMode,
                    ),
                )

            assertEquals(
                DeliveryResult.Delivered,
                result,
            )
        }
    }

    @Test
    fun `mini app action can be sent through library keyboard`() {
        val response = response(ok = true)

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                TelegramId(123L),
                RenderedMessage(
                    text = "open app",
                    actions =
                        listOf(
                            MessageAction.OpenMiniApp(
                                "Open Mnemos",
                            ),
                        ),
                ),
            )

        assertEquals(
            DeliveryResult.Delivered,
            result,
        )
    }

    private fun response(
        ok: Boolean,
        errorCode: Int = 0,
        description: String? = null,
    ): SendResponse {
        val response = mock<SendResponse>()

        whenever(response.isOk)
            .thenReturn(ok)

        whenever(response.errorCode())
            .thenReturn(errorCode)

        whenever(response.description())
            .thenReturn(description)

        return response
    }
}
