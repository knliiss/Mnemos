package com.mnemos.notification.adapter.inbound.telegram

import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.telegram.help.HandleTelegramHelpService
import com.mnemos.notification.application.telegram.help.TelegramHelpSection
import com.mnemos.notification.application.telegram.payment.HandleTelegramStarPaymentUpdateService
import com.mnemos.notification.application.telegram.start.HandleTelegramStartService
import com.mnemos.notification.application.telegram.start.TelegramStartContext
import com.pengrad.telegrambot.ExceptionHandler
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.ApplicationArguments

class TelegramLongPollingConsumerTest {
    private val bot = mock<TelegramBot>()
    private val startService = mock<HandleTelegramStartService>()
    private val helpService = mock<HandleTelegramHelpService>()
    private val starPaymentService = mock<HandleTelegramStarPaymentUpdateService>()
    private val consumer =
        TelegramLongPollingConsumer(
            bot = bot,
            startService = startService,
            helpService = helpService,
            starPaymentService = starPaymentService,
        )

    @Test
    fun `run delegates long polling lifecycle to telegram library`() {
        consumer.run(mock<ApplicationArguments>())

        verify(bot).setUpdatesListener(any<UpdatesListener>(), any<ExceptionHandler>())
    }

    @Test
    fun `non message update is confirmed and ignored`() {
        val update = update(id = 10)

        assertEquals(10, consumer.processUpdates(listOf(update)))
        verify(startService, never()).handle(any())
        verify(helpService, never()).send(any(), any())
    }

    @Test
    fun `non private start is confirmed and ignored`() {
        val update = update(id = 11, message = message(text = "/start", chatType = Chat.Type.group))

        assertEquals(11, consumer.processUpdates(listOf(update)))
        verify(startService, never()).handle(any())
    }

    @Test
    fun `non start private message is confirmed and ignored`() {
        val update = update(id = 12, message = message(text = "hello"))

        assertEquals(12, consumer.processUpdates(listOf(update)))
        verify(startService, never()).handle(any())
        verify(helpService, never()).send(any(), any())
    }

    @Test
    fun `start command is mapped to context and confirmed after delivery`() {
        val update =
            update(
                id = 13,
                message =
                    message(
                        text = "/start referral-42",
                        telegramId = 123456789L,
                        firstName = "Knalis",
                        username = "knalis",
                    ),
            )
        whenever(startService.handle(any())).thenReturn(DeliveryResult.Delivered)

        assertEquals(13, consumer.processUpdates(listOf(update)))
        verify(startService).handle(
            eq(
                TelegramStartContext(
                    telegramId = 123456789L,
                    firstName = "Knalis",
                    username = "knalis",
                    parameter = "referral-42",
                ),
            ),
        )
    }

    @Test
    fun `help command opens overview section`() {
        val update = update(id = 14, message = message(text = "/help", telegramId = 777L))
        whenever(helpService.send(any(), any())).thenReturn(DeliveryResult.Delivered)

        assertEquals(14, consumer.processUpdates(listOf(update)))
        verify(helpService).send(
            telegramId = 777L,
            section = TelegramHelpSection.OVERVIEW,
        )
    }

    @Test
    fun `help callback edits existing message and is acknowledged`() {
        val callbackMessage =
            message(
                text = "help",
                telegramId = 777L,
                messageId = 321,
            )
        val callbackQuery =
            mock<CallbackQuery> {
                on { id() }.thenReturn("callback-1")
                on { data() }.thenReturn(TelegramHelpSection.ACCOUNTS.callbackData)
                on { message() }.thenReturn(callbackMessage)
            }
        val update = update(id = 15, callbackQuery = callbackQuery)

        whenever(
            helpService.switchSection(
                telegramId = 777L,
                messageId = 321,
                section = TelegramHelpSection.ACCOUNTS,
            ),
        ).thenReturn(DeliveryResult.Delivered)

        assertEquals(15, consumer.processUpdates(listOf(update)))
        verify(helpService).switchSection(
            telegramId = 777L,
            messageId = 321,
            section = TelegramHelpSection.ACCOUNTS,
        )
        verify(bot).execute(any<AnswerCallbackQuery>())
    }

    @Test
    fun `start command addressed to bot is accepted`() {
        val update = update(id = 16, message = message(text = "/start@MnemosBot"))
        whenever(startService.handle(any())).thenReturn(DeliveryResult.Delivered)

        assertEquals(16, consumer.processUpdates(listOf(update)))
        verify(startService).handle(any())
    }

    @Test
    fun `retryable result leaves current update unconfirmed`() {
        val update = update(id = 17, message = message(text = "/start"))
        whenever(startService.handle(any())).thenReturn(DeliveryResult.Retryable())

        assertEquals(UpdatesListener.CONFIRMED_UPDATES_NONE, consumer.processUpdates(listOf(update)))
    }

    @Test
    fun `retryable result confirms only previous successful update`() {
        val first = update(id = 20, message = message(text = "ignored"))
        val second = update(id = 21, message = message(text = "/start"))
        whenever(startService.handle(any())).thenReturn(DeliveryResult.Retryable())

        assertEquals(20, consumer.processUpdates(listOf(first, second)))
    }

    @Test
    fun `permanent failure is confirmed to avoid poison update loop`() {
        val update = update(id = 22, message = message(text = "/start"))
        whenever(startService.handle(any())).thenReturn(DeliveryResult.PermanentFailure("blocked"))

        assertEquals(22, consumer.processUpdates(listOf(update)))
    }

    @Test
    fun `destroy stops library polling listener`() {
        consumer.destroy()

        verify(bot).removeGetUpdatesListener()
    }

    private fun update(
        id: Int,
        message: Message? = null,
        callbackQuery: CallbackQuery? = null,
    ): Update =
        mock<Update> {
            on { updateId() }.thenReturn(id)
            on { this.message() }.thenReturn(message)
            on { this.callbackQuery() }.thenReturn(callbackQuery)
        }

    private fun message(
        text: String,
        chatType: Chat.Type = Chat.Type.Private,
        telegramId: Long = 100L,
        messageId: Int = 1,
        firstName: String? = null,
        username: String? = null,
    ): Message {
        val chat =
            mock<Chat> {
                on { id() }.thenReturn(telegramId)
                on { type() }.thenReturn(chatType)
            }
        val user =
            mock<User> {
                on { firstName() }.thenReturn(firstName)
                on { username() }.thenReturn(username)
            }

        return mock {
            on { this.text() }.thenReturn(text)
            on { this.chat() }.thenReturn(chat)
            on { this.messageId() }.thenReturn(messageId)
            on { from() }.thenReturn(user)
        }
    }
}
