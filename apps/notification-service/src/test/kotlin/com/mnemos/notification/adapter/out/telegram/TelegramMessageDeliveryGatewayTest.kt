package com.mnemos.notification.adapter.out.telegram

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.MessageParseMode
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.configuration.telegram.TelegramBotProperties
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.EditMessageText
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.response.SendResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
        val response = sendResponse(ok = true)

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                telegramId = TelegramId(123L),
                message = RenderedMessage("hello"),
            )

        assertEquals(DeliveryResult.Delivered, result)
    }

    @Test
    fun `telegram 429 is retryable`() {
        val response =
            sendResponse(
                ok = false,
                errorCode = 429,
                description = "Too Many Requests",
            )

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                telegramId = TelegramId(123L),
                message = RenderedMessage("hello"),
            )

        assertInstanceOf(DeliveryResult.Retryable::class.java, result)
    }

    @Test
    fun `telegram client error is permanent failure`() {
        val response =
            sendResponse(
                ok = false,
                errorCode = 403,
                description = "bot was blocked by the user",
            )

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                telegramId = TelegramId(123L),
                message = RenderedMessage("hello"),
            )

        assertEquals(
            DeliveryResult.PermanentFailure("TELEGRAM_BLOCKED"),
            result,
        )
    }

    @Test
    fun `telegram exception is retryable`() {
        whenever(bot.execute(any<SendMessage>()))
            .thenThrow(IllegalStateException("network"))

        val result =
            gateway.deliver(
                telegramId = TelegramId(123L),
                message = RenderedMessage("hello"),
            )

        assertInstanceOf(DeliveryResult.Retryable::class.java, result)
    }

    @Test
    fun `all supported parse modes can be sent`() {
        val response = sendResponse(ok = true)

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        MessageParseMode.entries.forEach { parseMode ->
            val result =
                gateway.deliver(
                    telegramId = TelegramId(123L),
                    message =
                        RenderedMessage(
                            text = "hello",
                            parseMode = parseMode,
                        ),
                )

            assertEquals(DeliveryResult.Delivered, result)
        }
    }

    @Test
    fun `mini app button is attached to the original send request`() {
        val response = sendResponse(ok = true)

        whenever(bot.execute(any<SendMessage>()))
            .thenReturn(response)

        val result =
            gateway.deliver(
                telegramId = TelegramId(123L),
                message =
                    RenderedMessage(
                        text = "open app",
                        actions =
                            listOf(
                                MessageAction.OpenMiniApp("Open Mnemos"),
                            ),
                    ),
            )

        assertEquals(DeliveryResult.Delivered, result)

        val requestCaptor = argumentCaptor<SendMessage>()
        verify(bot).execute(requestCaptor.capture())

        assertInstanceOf(
            InlineKeyboardMarkup::class.java,
            requestCaptor.firstValue.parameters["reply_markup"],
        )
    }

    @Test
    fun `help section can edit existing telegram message`() {
        val response = baseResponse(ok = true)

        whenever(bot.execute(any<EditMessageText>()))
            .thenReturn(response)

        val result =
            gateway.edit(
                telegramId = TelegramId(123L),
                messageId = 321,
                message =
                    RenderedMessage(
                        text = "help",
                        actions =
                            listOf(
                                MessageAction.Callback(
                                    label = "Accounts",
                                    data = "help:accounts",
                                ),
                            ),
                    ),
            )

        assertEquals(DeliveryResult.Delivered, result)

        val requestCaptor = argumentCaptor<EditMessageText>()
        verify(bot).execute(requestCaptor.capture())

        assertEquals(123L, requestCaptor.firstValue.parameters["chat_id"])
        assertEquals(321, requestCaptor.firstValue.parameters["message_id"])
        assertInstanceOf(
            InlineKeyboardMarkup::class.java,
            requestCaptor.firstValue.parameters["reply_markup"],
        )
    }

    private fun sendResponse(
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

    private fun baseResponse(
        ok: Boolean,
        errorCode: Int = 0,
        description: String? = null,
    ): BaseResponse {
        val response = mock<BaseResponse>()

        whenever(response.isOk)
            .thenReturn(ok)
        whenever(response.errorCode())
            .thenReturn(errorCode)
        whenever(response.description())
            .thenReturn(description)

        return response
    }
}
