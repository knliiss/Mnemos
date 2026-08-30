package com.mnemos.telegram.bot.application.input

import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramTextInputRouter(
    private val sessionRepository: TelegramMenuSessionRepository,
    handlers: List<TelegramTextInputHandler>,
) {
    private val handlersByInput = handlers.associateBy { handler -> handler.pendingInput }

    init {
        require(handlersByInput.size == handlers.size) {
            "Only one Telegram text input handler can be registered for an input type"
        }
    }

    fun route(context: TelegramTextInputContext): TelegramDeliveryResult {
        val pendingInput = sessionRepository.find(context.telegramId)?.pendingInput
            ?: return TelegramDeliveryResult.Delivered
        val handler = handlersByInput[pendingInput]
            ?: return TelegramDeliveryResult.Delivered

        return handler.handle(context)
    }

    internal fun registeredInputs(): Set<TelegramPendingInput> = handlersByInput.keys
}
