package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramCallbackRouter
import com.mnemos.telegram.bot.application.command.TelegramCommandContext
import com.mnemos.telegram.bot.application.command.TelegramCommandRouter
import com.mnemos.telegram.bot.application.input.TelegramTextInputContext
import com.mnemos.telegram.bot.application.input.TelegramTextInputRouter
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.payment.HandleTelegramStarPaymentUpdateService
import com.pengrad.telegrambot.ExceptionHandler
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.PreCheckoutQuery
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
    private val commandRouter = mock<TelegramCommandRouter>()
    private val textInputRouter = mock<TelegramTextInputRouter>()
    private val callbackRouter = mock<TelegramCallbackRouter>()
    private val starPaymentService = mock<HandleTelegramStarPaymentUpdateService>()
    private val consumer =
        TelegramLongPollingConsumer(
            bot = bot,
            commandRouter = commandRouter,
            textInputRouter = textInputRouter,
            callbackRouter = callbackRouter,
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

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(10, confirmedUpdateId)
        verify(commandRouter, never()).route(any())
        verify(textInputRouter, never()).route(any())
        verify(callbackRouter, never()).route(any())
    }

    @Test
    fun `private command is trimmed mapped and confirmed`() {
        val update =
            update(
                id = 11,
                message =
                    message(
                        text = "  /start referral-42  ",
                        telegramId = 123456789L,
                        firstName = "Knalis",
                        username = "knalis",
                    ),
            )
        val expectedContext =
            TelegramCommandContext(
                telegramId = 123456789L,
                text = "/start referral-42",
                firstName = "Knalis",
                username = "knalis",
            )
        whenever(commandRouter.route(any())).thenReturn(TelegramDeliveryResult.Delivered)

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(11, confirmedUpdateId)
        verify(commandRouter).route(eq(expectedContext))
        verify(textInputRouter, never()).route(any())
    }

    @Test
    fun `private text is trimmed and mapped to text input router`() {
        val update =
            update(
                id = 12,
                message = message(text = "  search query  ", telegramId = 777L),
            )
        val expectedContext =
            TelegramTextInputContext(
                telegramId = 777L,
                text = "search query",
            )
        whenever(textInputRouter.route(any())).thenReturn(TelegramDeliveryResult.Delivered)

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(12, confirmedUpdateId)
        verify(textInputRouter).route(eq(expectedContext))
        verify(commandRouter, never()).route(any())
    }

    @Test
    fun `non private user message is confirmed and ignored`() {
        val update =
            update(
                id = 13,
                message =
                    message(
                        text = "/start",
                        chatType = Chat.Type.group,
                    ),
            )

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(13, confirmedUpdateId)
        verify(commandRouter, never()).route(any())
        verify(textInputRouter, never()).route(any())
    }

    @Test
    fun `callback is acknowledged and routed for matching private actor`() {
        val callbackMessage =
            message(
                text = "menu",
                telegramId = 777L,
                messageId = 321,
            )
        val callbackQuery =
            callbackQuery(
                message = callbackMessage,
                actorId = 777L,
                data = "menu:home",
            )
        val update = update(id = 14, callbackQuery = callbackQuery)
        val expectedContext =
            TelegramCallbackContext(
                telegramId = 777L,
                messageId = 321,
                data = "menu:home",
            )
        whenever(callbackRouter.route(any())).thenReturn(TelegramDeliveryResult.Delivered)

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(14, confirmedUpdateId)
        verify(bot).execute(any<AnswerCallbackQuery>())
        verify(callbackRouter).route(eq(expectedContext))
    }

    @Test
    fun `callback with mismatched private actor is acknowledged but not routed`() {
        val callbackMessage =
            message(
                text = "menu",
                telegramId = 777L,
                messageId = 321,
            )
        val callbackQuery =
            callbackQuery(
                message = callbackMessage,
                actorId = 778L,
                data = "menu:home",
            )
        val update = update(id = 15, callbackQuery = callbackQuery)

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(15, confirmedUpdateId)
        verify(bot).execute(any<AnswerCallbackQuery>())
        verify(callbackRouter, never()).route(any())
    }

    @Test
    fun `pre checkout update is routed before message handling`() {
        val preCheckoutQuery = mock<PreCheckoutQuery>()
        val update = update(id = 16, preCheckoutQuery = preCheckoutQuery)
        whenever(starPaymentService.handlePreCheckout(preCheckoutQuery)).thenReturn(TelegramDeliveryResult.Delivered)

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(16, confirmedUpdateId)
        verify(starPaymentService).handlePreCheckout(preCheckoutQuery)
        verify(commandRouter, never()).route(any())
        verify(textInputRouter, never()).route(any())
        verify(callbackRouter, never()).route(any())
    }

    @Test
    fun `retryable result leaves current update unconfirmed`() {
        val update = update(id = 17, message = message(text = "/start"))
        whenever(commandRouter.route(any())).thenReturn(TelegramDeliveryResult.Retryable)

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(UpdatesListener.CONFIRMED_UPDATES_NONE, confirmedUpdateId)
    }

    @Test
    fun `retryable result confirms only previous successful update`() {
        val first = update(id = 20)
        val second = update(id = 21, message = message(text = "/start"))
        whenever(commandRouter.route(any())).thenReturn(TelegramDeliveryResult.Retryable)

        val confirmedUpdateId = consumer.processUpdates(listOf(first, second))

        assertEquals(20, confirmedUpdateId)
    }

    @Test
    fun `permanent failure is confirmed to avoid poison update loop`() {
        val update = update(id = 22, message = message(text = "/start"))
        whenever(commandRouter.route(any())).thenReturn(
            TelegramDeliveryResult.PermanentFailure("blocked"),
        )

        val confirmedUpdateId = consumer.processUpdates(listOf(update))

        assertEquals(22, confirmedUpdateId)
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
        preCheckoutQuery: PreCheckoutQuery? = null,
    ): Update =
        mock<Update> {
            on { updateId() }.thenReturn(id)
            on { this.message() }.thenReturn(message)
            on { this.callbackQuery() }.thenReturn(callbackQuery)
            on { this.preCheckoutQuery() }.thenReturn(preCheckoutQuery)
        }

    private fun message(
        text: String? = null,
        chatType: Chat.Type = Chat.Type.Private,
        telegramId: Long = 100L,
        actorId: Long = telegramId,
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
                on { id() }.thenReturn(actorId)
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

    private fun callbackQuery(
        message: Message,
        actorId: Long,
        data: String,
    ): CallbackQuery {
        val user =
            mock<User> {
                on { id() }.thenReturn(actorId)
            }

        return mock {
            on { id() }.thenReturn("callback-1")
            on { this.message() }.thenReturn(message)
            on { from() }.thenReturn(user)
            on { this.data() }.thenReturn(data)
        }
    }
}
