package com.mnemos.telegram.bot.application.payment

import com.mnemos.payment.contracts.internal.telegram.RecordSuccessfulStarPaymentRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutResponse
import com.mnemos.telegram.bot.adapter.out.payment.PaymentServiceClient
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.PreCheckoutQuery
import com.pengrad.telegrambot.model.SuccessfulPayment
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery
import com.pengrad.telegrambot.response.BaseResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class HandleTelegramStarPaymentUpdateServiceTest {
    private val bot = mock<TelegramBot>()
    private val paymentServiceClient = mock<PaymentServiceClient>()
    private val service =
        HandleTelegramStarPaymentUpdateService(
            bot = bot,
            paymentServiceClient = paymentServiceClient,
        )

    @Test
    fun `approved pre checkout is forwarded and acknowledged in Telegram`() {
        val query = preCheckoutQuery()
        val telegramResponse = successfulTelegramResponse()

        whenever(
            paymentServiceClient.validateStarPreCheckout(
                ValidateStarPreCheckoutRequest(
                    telegramId = TELEGRAM_ID,
                    invoicePayload = INVOICE_PAYLOAD,
                    currency = "XTR",
                    totalAmount = STARS_AMOUNT,
                ),
            ),
        ).thenReturn(
            ValidateStarPreCheckoutResponse(
                approved = true,
                errorMessage = null,
            ),
        )
        whenever(bot.execute(any<AnswerPreCheckoutQuery>())).thenReturn(telegramResponse)

        val result = service.handlePreCheckout(query)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(bot).execute(any<AnswerPreCheckoutQuery>())
    }

    @Test
    fun `rejected pre checkout is answered without treating it as transport failure`() {
        val query = preCheckoutQuery()
        val telegramResponse = successfulTelegramResponse()

        whenever(paymentServiceClient.validateStarPreCheckout(any())).thenReturn(
            ValidateStarPreCheckoutResponse(
                approved = false,
                errorMessage = "Invoice expired",
            ),
        )
        whenever(bot.execute(any<AnswerPreCheckoutQuery>())).thenReturn(telegramResponse)

        val result = service.handlePreCheckout(query)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(bot).execute(any<AnswerPreCheckoutQuery>())
    }

    @Test
    fun `Telegram pre checkout answer failure keeps update retryable`() {
        val query = preCheckoutQuery()

        whenever(paymentServiceClient.validateStarPreCheckout(any())).thenReturn(
            ValidateStarPreCheckoutResponse(
                approved = true,
                errorMessage = null,
            ),
        )
        whenever(bot.execute(any<AnswerPreCheckoutQuery>())).thenThrow(
            IllegalStateException("Telegram unavailable"),
        )

        val result = service.handlePreCheckout(query)

        assertEquals(TelegramDeliveryResult.Retryable, result)
    }

    @Test
    fun `successful payment is forwarded with private Telegram actor identity`() {
        val message = successfulPaymentMessage()

        val result = service.handleSuccessfulPayment(message)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(paymentServiceClient).recordSuccessfulStarPayment(
            RecordSuccessfulStarPaymentRequest(
                telegramId = TELEGRAM_ID,
                invoicePayload = INVOICE_PAYLOAD,
                currency = "XTR",
                totalAmount = STARS_AMOUNT,
                telegramPaymentChargeId = PAYMENT_CHARGE_ID,
            ),
        )
    }

    @Test
    fun `successful payment from mismatched private actor is permanently rejected`() {
        val message =
            successfulPaymentMessage(
                chatId = TELEGRAM_ID,
                actorId = TELEGRAM_ID + 1,
            )

        val result = service.handleSuccessfulPayment(message)

        assertTrue(result is TelegramDeliveryResult.PermanentFailure)
        verifyNoInteractions(paymentServiceClient)
    }

    @Test
    fun `message without successful payment is ignored`() {
        val message = mock<Message>()

        val result = service.handleSuccessfulPayment(message)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(paymentServiceClient, never()).recordSuccessfulStarPayment(any())
    }

    private fun preCheckoutQuery(): PreCheckoutQuery {
        val user =
            mock<User> {
                on { id() }.thenReturn(TELEGRAM_ID)
            }

        return mock {
            on { id() }.thenReturn(PRE_CHECKOUT_QUERY_ID)
            on { from() }.thenReturn(user)
            on { invoicePayload() }.thenReturn(INVOICE_PAYLOAD)
            on { currency() }.thenReturn("XTR")
            on { totalAmount() }.thenReturn(STARS_AMOUNT)
        }
    }

    private fun successfulPaymentMessage(
        chatId: Long = TELEGRAM_ID,
        actorId: Long = TELEGRAM_ID,
    ): Message {
        val chat =
            mock<Chat> {
                on { id() }.thenReturn(chatId)
                on { type() }.thenReturn(Chat.Type.Private)
            }
        val user =
            mock<User> {
                on { id() }.thenReturn(actorId)
            }
        val payment =
            mock<SuccessfulPayment> {
                on { invoicePayload }.thenReturn(INVOICE_PAYLOAD)
                on { currency }.thenReturn("XTR")
                on { totalAmount }.thenReturn(STARS_AMOUNT)
                on { telegramPaymentChargeId }.thenReturn(PAYMENT_CHARGE_ID)
            }

        return mock {
            on { successfulPayment() }.thenReturn(payment)
            on { this.chat() }.thenReturn(chat)
            on { from() }.thenReturn(user)
        }
    }

    private fun successfulTelegramResponse(): BaseResponse =
        mock {
            on { isOk }.thenReturn(true)
        }

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val PRE_CHECKOUT_QUERY_ID = "pre-checkout-42"
        const val INVOICE_PAYLOAD = "mnemos-prime:test-payload"
        const val PAYMENT_CHARGE_ID = "telegram-charge-42"
        const val STARS_AMOUNT = 250
    }
}
