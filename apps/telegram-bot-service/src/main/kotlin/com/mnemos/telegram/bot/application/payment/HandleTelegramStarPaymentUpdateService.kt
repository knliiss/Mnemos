package com.mnemos.telegram.bot.application.payment

import com.mnemos.payment.contracts.internal.telegram.RecordSuccessfulStarPaymentRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutRequest
import com.mnemos.telegram.bot.adapter.out.payment.PaymentServiceClient
import com.mnemos.telegram.bot.application.identity.TelegramPrivateActorPolicy
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.PreCheckoutQuery
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class HandleTelegramStarPaymentUpdateService(
    private val bot: TelegramBot,
    private val paymentServiceClient: PaymentServiceClient,
) {
    fun handlePreCheckout(query: PreCheckoutQuery): TelegramDeliveryResult {
        val answer =
            try {
                val decision =
                    paymentServiceClient.validateStarPreCheckout(
                        ValidateStarPreCheckoutRequest(
                            telegramId = query.from().id(),
                            invoicePayload = query.invoicePayload(),
                            currency = query.currency(),
                            totalAmount = query.totalAmount(),
                        ),
                    )

                if (decision.approved) {
                    AnswerPreCheckoutQuery(query.id())
                } else {
                    AnswerPreCheckoutQuery(
                        query.id(),
                        decision.errorMessage ?: DEFAULT_REJECTION_MESSAGE,
                    )
                }
            } catch (exception: FeignException) {
                log.warn("Payment service failed while validating Telegram Stars pre-checkout", exception)

                if (!PaymentServiceFailurePolicy.isPermanent(exception.status())) {
                    return TelegramDeliveryResult.Retryable
                }

                AnswerPreCheckoutQuery(query.id(), DEFAULT_REJECTION_MESSAGE)
            }

        val response =
            try {
                bot.execute(answer)
            } catch (exception: RuntimeException) {
                log.warn("Failed to answer Telegram Stars pre-checkout query", exception)
                return TelegramDeliveryResult.Retryable
            }

        if (!response.isOk) {
            log.warn(
                "Telegram rejected pre-checkout answer: code={}, description={}",
                response.errorCode(),
                response.description(),
            )
            return TelegramDeliveryResult.Retryable
        }

        return TelegramDeliveryResult.Delivered
    }

    fun handleSuccessfulPayment(message: Message): TelegramDeliveryResult {
        val payment = message.successfulPayment() ?: return TelegramDeliveryResult.Delivered
        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = message.chat().type() == Chat.Type.Private,
                chatId = message.chat().id(),
                actorId = message.from()?.id(),
            ) ?: return TelegramDeliveryResult.PermanentFailure(
                "Telegram Stars payment actor does not match the private chat",
            )

        return try {
            paymentServiceClient.recordSuccessfulStarPayment(
                RecordSuccessfulStarPaymentRequest(
                    telegramId = telegramId,
                    invoicePayload = payment.invoicePayload,
                    currency = payment.currency,
                    totalAmount = payment.totalAmount,
                    telegramPaymentChargeId = payment.telegramPaymentChargeId,
                ),
            )

            TelegramDeliveryResult.Delivered
        } catch (exception: FeignException) {
            log.warn("Payment service failed while recording successful Telegram Stars payment", exception)

            if (PaymentServiceFailurePolicy.isPermanent(exception.status())) {
                TelegramDeliveryResult.PermanentFailure("Payment service rejected Telegram Stars payment")
            } else {
                TelegramDeliveryResult.Retryable
            }
        }
    }

    private companion object {
        const val DEFAULT_REJECTION_MESSAGE = "Не удалось подтвердить платёж. Создайте новый счёт в Mnemos."
        val log = LoggerFactory.getLogger(HandleTelegramStarPaymentUpdateService::class.java)
    }
}
