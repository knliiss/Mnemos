package com.mnemos.notification.application.telegram.payment

import com.mnemos.notification.adapter.out.payment.PaymentServiceClient
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.payment.contracts.internal.telegram.RecordSuccessfulStarPaymentRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutRequest
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.PreCheckoutQuery
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
class HandleTelegramStarPaymentUpdateService(
    private val bot: TelegramBot,
    private val paymentServiceClient: PaymentServiceClient,
) {
    fun handlePreCheckout(query: PreCheckoutQuery): DeliveryResult {
        val decision =
            try {
                paymentServiceClient.validateStarPreCheckout(
                    ValidateStarPreCheckoutRequest(
                        telegramId = query.from().id(),
                        invoicePayload = query.invoicePayload(),
                        currency = query.currency(),
                        totalAmount = query.totalAmount(),
                    ),
                )
            } catch (exception: FeignException) {
                log.warn("Payment service failed while validating Telegram Stars pre-checkout", exception)
                return DeliveryResult.Retryable()
            }

        val answer =
            if (decision.approved) {
                AnswerPreCheckoutQuery(query.id())
            } else {
                AnswerPreCheckoutQuery(
                    query.id(),
                    decision.errorMessage ?: DEFAULT_REJECTION_MESSAGE,
                )
            }

        val response =
            try {
                bot.execute(answer)
            } catch (exception: RuntimeException) {
                log.warn("Failed to answer Telegram Stars pre-checkout query", exception)
                return DeliveryResult.Retryable()
            }

        if (!response.isOk) {
            log.warn(
                "Telegram rejected pre-checkout answer: code={}, description={}",
                response.errorCode(),
                response.description(),
            )
            return DeliveryResult.Retryable()
        }

        return DeliveryResult.Delivered
    }

    fun handleSuccessfulPayment(message: Message): DeliveryResult {
        val payment = message.successfulPayment() ?: return DeliveryResult.Delivered

        if (message.chat().type() != com.pengrad.telegrambot.model.Chat.Type.Private) {
            return DeliveryResult.PermanentFailure("Telegram Stars payment arrived outside a private chat")
        }

        return try {
            paymentServiceClient.recordSuccessfulStarPayment(
                RecordSuccessfulStarPaymentRequest(
                    telegramId = message.chat().id(),
                    invoicePayload = payment.invoicePayload,
                    currency = payment.currency,
                    totalAmount = payment.totalAmount,
                    telegramPaymentChargeId = payment.telegramPaymentChargeId,
                ),
            )

            DeliveryResult.Delivered
        } catch (exception: FeignException) {
            log.warn("Payment service failed while recording successful Telegram Stars payment", exception)
            DeliveryResult.Retryable()
        }
    }

    private companion object {
        const val DEFAULT_REJECTION_MESSAGE = "Не удалось подтвердить платёж. Создайте новый счёт в Mnemos."
        val log = LoggerFactory.getLogger(HandleTelegramStarPaymentUpdateService::class.java)
    }
}
