package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramCallbackRouter
import com.mnemos.telegram.bot.application.command.TelegramCommandContext
import com.mnemos.telegram.bot.application.command.TelegramCommandRouter
import com.mnemos.telegram.bot.application.identity.TelegramPrivateActorPolicy
import com.mnemos.telegram.bot.application.input.TelegramTextInputContext
import com.mnemos.telegram.bot.application.input.TelegramTextInputRouter
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.payment.HandleTelegramStarPaymentUpdateService
import com.pengrad.telegrambot.ExceptionHandler
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.SocketTimeoutException

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramLongPollingConsumer(
    private val bot: TelegramBot,
    private val commandRouter: TelegramCommandRouter,
    private val textInputRouter: TelegramTextInputRouter,
    private val callbackRouter: TelegramCallbackRouter,
    private val starPaymentService: HandleTelegramStarPaymentUpdateService,
) : ApplicationRunner,
    DisposableBean {
    override fun run(args: ApplicationArguments) {
        bot.setUpdatesListener(
            UpdatesListener(::processUpdates),
            ExceptionHandler(::handlePollingException),
        )
    }

    override fun destroy() {
        bot.removeGetUpdatesListener()
    }

    private var retryUpdateId: Int? = null
    private var lastRetryLogNanos: Long = 0L

    internal fun processUpdates(updates: List<Update>): Int {
        var lastConfirmed = UpdatesListener.CONFIRMED_UPDATES_NONE

        for (update in updates) {
            val (result, failureType) =
                try {
                    handle(update) to null
                } catch (exception: Exception) {
                    // Unhandled listener exceptions terminate pengrad's polling callback.
                    // Do not acknowledge the update; retry after a bounded pause.
                    TelegramDeliveryResult.Retryable to exception.javaClass.simpleName
                }

            when (result) {
                TelegramDeliveryResult.Delivered,
                is TelegramDeliveryResult.PermanentFailure,
                -> {
                    lastConfirmed = update.updateId()
                    retryUpdateId = null
                }

                TelegramDeliveryResult.Retryable -> {
                    pauseAndReportRetry(update.updateId(), failureType)
                    return lastConfirmed
                }
            }
        }

        return lastConfirmed
    }

    private fun pauseAndReportRetry(
        updateId: Int,
        failureType: String?,
    ) {
        val now = System.nanoTime()
        if (retryUpdateId != updateId || now - lastRetryLogNanos >= RETRY_LOG_INTERVAL_NANOS) {
            log.warn(
                "Telegram update processing retry: updateId={}, failureType={}",
                updateId,
                failureType ?: "retryable-result",
            )
            retryUpdateId = updateId
            lastRetryLogNanos = now
        }

        try {
            Thread.sleep(RETRY_BACKOFF_MILLIS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    internal fun handle(update: Update): TelegramDeliveryResult {
        val preCheckoutQuery = update.preCheckoutQuery()

        if (preCheckoutQuery != null) {
            return starPaymentService.handlePreCheckout(preCheckoutQuery)
        }

        val callbackQuery = update.callbackQuery()

        if (callbackQuery != null) {
            acknowledgeCallback(callbackQuery)
            return handleCallback(callbackQuery)
        }

        val message = update.message() ?: return TelegramDeliveryResult.Delivered

        if (message.successfulPayment() != null) {
            return starPaymentService.handleSuccessfulPayment(message)
        }

        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = message.chat().type() == Chat.Type.Private,
                chatId = message.chat().id(),
                actorId = message.from()?.id(),
            ) ?: return TelegramDeliveryResult.Delivered
        val text = message.text()?.trim() ?: return TelegramDeliveryResult.Delivered

        if (text.startsWith('/')) {
            return commandRouter.route(
                TelegramCommandContext(
                    telegramId = telegramId,
                    text = text,
                    firstName = message.from()?.firstName(),
                    username = message.from()?.username(),
                ),
            )
        }

        return textInputRouter.route(
            TelegramTextInputContext(
                telegramId = telegramId,
                text = text,
            ),
        )
    }

    private fun handleCallback(callbackQuery: CallbackQuery): TelegramDeliveryResult {
        val message = callbackQuery.maybeInaccessibleMessage() ?: return TelegramDeliveryResult.Delivered
        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = message.chat().type() == Chat.Type.Private,
                chatId = message.chat().id(),
                actorId = callbackQuery.from().id(),
            ) ?: return TelegramDeliveryResult.Delivered

        return callbackRouter.route(
            TelegramCallbackContext(
                telegramId = telegramId,
                messageId = message.messageId(),
                callbackQueryId = callbackQuery.id(),
                data = callbackQuery.data().orEmpty(),
            ),
        )
    }

    private fun acknowledgeCallback(callbackQuery: CallbackQuery) {
        try {
            bot.execute(AnswerCallbackQuery(callbackQuery.id()))
        } catch (exception: RuntimeException) {
            log.debug("Failed to acknowledge Telegram callback query", exception)
        }
    }

    private fun handlePollingException(exception: TelegramException) {
        val response = exception.response()

        if (response != null) {
            log.warn(
                "Telegram long polling failed: code={}, description={}",
                response.errorCode(),
                response.description(),
            )
            return
        }

        val cause = exception.cause

        if (cause is SocketTimeoutException) {
            log.debug("Telegram long polling timed out, retrying")
            return
        }

        log.warn("Telegram long polling failed", exception)
    }

    private companion object {
        const val RETRY_BACKOFF_MILLIS = 500L
        const val RETRY_LOG_INTERVAL_NANOS = 60_000_000_000L
        val log = LoggerFactory.getLogger(TelegramLongPollingConsumer::class.java)
    }
}
