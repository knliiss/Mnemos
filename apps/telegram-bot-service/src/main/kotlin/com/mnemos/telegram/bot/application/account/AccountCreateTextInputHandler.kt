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

        return when (val result = gateway.createAccount(context.telegramId, nickname)) {
            TelegramAccountMutationResult.Updated ->
                menuService.completeTextInput(
                    telegramId = context.telegramId,
                    expectedScreen = TelegramMenuScreen.ACCOUNT,
                    expectedInput = pendingInput,
                ) {
                    AccountMenuContext.created()
                }

            is TelegramAccountMutationResult.Invalid ->
                updatePending(
                    telegramId = context.telegramId,
                    screenContext = createFailureContext(result.code),
                )

            TelegramAccountMutationResult.Unavailable ->
                updatePending(context.telegramId, AccountMenuContext.unavailable())
        }
    }

    private fun createFailureContext(code: String?): Map<String, String> =
        when (code) {
            ACCOUNT_IDENTITY_CONFLICT -> AccountMenuContext.createIdentityConflict()
            CRISTALIX_PLAYER_NOT_FOUND -> AccountMenuContext.createPlayerNotFound()
            else -> AccountMenuContext.createInvalid()
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

    private companion object {
        const val ACCOUNT_IDENTITY_CONFLICT = "ACCOUNT_IDENTITY_CONFLICT"
        const val CRISTALIX_PLAYER_NOT_FOUND = "CRISTALIX_PLAYER_NOT_FOUND"
    }
}
