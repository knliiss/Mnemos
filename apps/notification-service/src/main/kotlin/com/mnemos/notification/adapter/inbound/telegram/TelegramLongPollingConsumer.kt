package com.mnemos.notification.adapter.inbound.telegram

import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.telegram.start.HandleTelegramStartService
import com.mnemos.notification.application.telegram.start.TelegramStartContext
import com.pengrad.telegrambot.ExceptionHandler
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
class TelegramLongPollingConsumer(
    private val bot: TelegramBot,
    private val startService: HandleTelegramStartService,
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
        val message = update.message() ?: return DeliveryResult.Delivered
        val text = message.text()?.trim() ?: return DeliveryResult.Delivered
        if (message.chat().type() != Chat.Type.Private) return DeliveryResult.Delivered

        val command = text.substringBefore(' ')
        if (command != "/start" && !command.startsWith("/start@")) {
            return DeliveryResult.Delivered
        }

        return startService.handle(
            TelegramStartContext(
                telegramId = message.chat().id(),
                firstName = message.from()?.firstName(),
                username = message.from()?.username(),
                parameter = text.substringAfter(' ', "").trim().takeIf(String::isNotEmpty),
            ),
        )
    }

    private fun handlePollingException(exception: TelegramException) {
        val response = exception.response()

        if (response != null) {
            log.warn(
                "Telegram long polling failed: code={}, description={}",
                response.errorCode(),
                response.description(),
            )
        } else {
            log.warn("Telegram long polling failed", exception)
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(TelegramLongPollingConsumer::class.java)
    }
}
