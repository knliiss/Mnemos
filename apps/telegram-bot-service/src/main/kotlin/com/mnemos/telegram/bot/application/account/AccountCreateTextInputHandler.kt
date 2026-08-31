package com.mnemos.telegram.bot.application.account

import com.mnemos.telegram.bot.application.account.model.TelegramAccountMutationResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.input.TelegramTextInputContext
import com.mnemos.telegram.bot.application.input.TelegramTextInputHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountCreateTextInputHandler(
    private val gateway: AccountGateway,
    private val menuService: TelegramMenuService,
) : TelegramTextInputHandler {
    override val pendingInput: TelegramPendingInput = TelegramPendingInput.ACCOUNT_CREATE

    override fun handle(context: TelegramTextInputContext): TelegramDeliveryResult {
        val nickname = context.text.trim()

        if (nickname.isBlank()) {
            return updatePending(context.telegramId, AccountMenuContext.createInvalid())
        }

        return when (gateway.createAccount(context.telegramId, nickname)) {
            TelegramAccountMutationResult.Updated ->
                menuService.completeTextInput(
                    telegramId = context.telegramId,
                    expectedScreen = TelegramMenuScreen.ACCOUNT,
                    expectedInput = pendingInput,
                ) {
                    AccountMenuContext.created()
                }

            TelegramAccountMutationResult.Invalid ->
                updatePending(context.telegramId, AccountMenuContext.createInvalid())

            TelegramAccountMutationResult.Unavailable ->
                updatePending(context.telegramId, AccountMenuContext.unavailable())
        }
    }

    private fun updatePending(
        telegramId: Long,
        screenContext: Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updatePendingTextInputContext(
            telegramId = telegramId,
            expectedScreen = TelegramMenuScreen.ACCOUNT,
            expectedInput = pendingInput,
            updateContext = { screenContext },
        )
}
