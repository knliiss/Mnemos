package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramCallbackRouter
import com.mnemos.telegram.bot.application.command.TelegramCommandContext
import com.mnemos.telegram.bot.application.command.TelegramCommandRouter
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

    internal fun processUpdates(updates: List<Update>): Int {
        var lastConfirmed = UpdatesListener.CONFIRMED_UPDATES_NONE

        for (update in updates) {
            when (handle(update)) {
                TelegramDeliveryResult.Delivered,
                is TelegramDeliveryResult.PermanentFailure,
                -> lastConfirmed = update.updateId()

                TelegramDeliveryResult.Retryable -> return lastConfirmed
            }
        }

        return lastConfirmed
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

        if (message.chat().type() != Chat.Type.Private) {
            return TelegramDeliveryResult.Delivered
        }

        val text = message.text()?.trim() ?: return TelegramDeliveryResult.Delivered

        return commandRouter.route(
            TelegramCommandContext(
                telegramId = message.chat().id(),
                text = text,
                firstName = message.from()?.firstName(),
                username = message.from()?.username(),
            ),
        )
    }

    private fun handleCallback(callbackQuery: CallbackQuery): TelegramDeliveryResult {
        val message = callbackQuery.message() ?: return TelegramDeliveryResult.Delivered

        if (message.chat().type() != Chat.Type.Private) {
            return TelegramDeliveryResult.Delivered
        }

        return callbackRouter.route(
            TelegramCallbackContext(
                telegramId = message.chat().id(),
                messageId = message.messageId(),
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
        val log = LoggerFactory.getLogger(TelegramLongPollingConsumer::class.java)
    }
}
