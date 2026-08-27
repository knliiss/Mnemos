package com.mnemos.notification.adapter.inbound.telegram

import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.telegram.help.HandleTelegramHelpService
import com.mnemos.notification.application.telegram.help.TelegramHelpSection
import com.mnemos.notification.application.telegram.payment.HandleTelegramStarPaymentUpdateService
import com.mnemos.notification.application.telegram.start.HandleTelegramStartService
import com.mnemos.notification.application.telegram.start.TelegramStartContext
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
@ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
class TelegramLongPollingConsumer(
    private val bot: TelegramBot,
    private val startService: HandleTelegramStartService,
    private val helpService: HandleTelegramHelpService,
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
                DeliveryResult.Delivered,
                is DeliveryResult.PermanentFailure,
                -> lastConfirmed = update.updateId()

                is DeliveryResult.Retryable -> return lastConfirmed
            }
        }

        return lastConfirmed
    }

    internal fun handle(update: Update): DeliveryResult {
        val preCheckoutQuery = update.preCheckoutQuery()

        if (preCheckoutQuery != null) {
            return starPaymentService.handlePreCheckout(preCheckoutQuery)
        }

        val callbackQuery = update.callbackQuery()

        if (callbackQuery != null) {
            acknowledgeCallback(callbackQuery)
            return handleCallback(callbackQuery)
        }

        val message = update.message() ?: return DeliveryResult.Delivered

        if (message.successfulPayment() != null) {
            return starPaymentService.handleSuccessfulPayment(message)
        }

        val text = message.text()?.trim() ?: return DeliveryResult.Delivered

        if (message.chat().type() != Chat.Type.Private) {
            return DeliveryResult.Delivered
        }

        val command = text.substringBefore(' ')

        if (command == "/start" || command.startsWith("/start@")) {
            return startService.handle(
                TelegramStartContext(
                    telegramId = message.chat().id(),
                    firstName = message.from()?.firstName(),
                    username = message.from()?.username(),
                    parameter = text.substringAfter(' ', "").trim().takeIf(String::isNotEmpty),
                ),
            )
        }

        if (command == "/help" || command.startsWith("/help@")) {
            return helpService.send(message.chat().id())
        }

        return DeliveryResult.Delivered
    }

    private fun handleCallback(callbackQuery: CallbackQuery): DeliveryResult {
        val section =
            callbackQuery
                .data()
                ?.let(TelegramHelpSection::fromCallbackData)
                ?: return DeliveryResult.Delivered
        val message = callbackQuery.message() ?: return DeliveryResult.Delivered

        if (message.chat().type() != Chat.Type.Private) {
            return DeliveryResult.Delivered
        }

        return helpService.switchSection(
            telegramId = message.chat().id(),
            messageId = message.messageId(),
            section = section,
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
