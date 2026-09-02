package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.input.TelegramTextInputContext
import com.mnemos.telegram.bot.application.input.TelegramTextInputHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuFeedback
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class NotificationItemDropItemTextInputHandler(
    private val menuService: TelegramMenuService,
) : TelegramTextInputHandler {
    override val pendingInput: TelegramPendingInput = TelegramPendingInput.NOTIFICATION_ITEM_DROP_ITEM

    override fun handle(context: TelegramTextInputContext): TelegramDeliveryResult {
        val itemName = NotificationItemDropInputValidator.item(context.text)

        if (itemName == null) {
            return menuService.updatePendingTextInputContext(
                telegramId = context.telegramId,
                expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
                expectedInput = pendingInput,
            ) { current ->
                NotificationMenuContext.withFeedback(current, NotificationMenuFeedback.INVALID_ITEM_FILTER)
            }
        }

        return menuService.completeTextInput(
            telegramId = context.telegramId,
            expectedScreen = TelegramMenuScreen.NOTIFICATIONS,
            expectedInput = pendingInput,
        ) { current ->
            NotificationMenuContext.withItemName(current, itemName)
        }
    }
}
