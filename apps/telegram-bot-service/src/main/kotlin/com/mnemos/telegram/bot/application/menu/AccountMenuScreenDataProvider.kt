package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.account.AccountMenuContext
import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountInputMode
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import org.springframework.stereotype.Component

@Component
class AccountMenuScreenDataProvider(
    private val accountGateway: AccountGateway,
    private val balanceGateway: AccountBalanceGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ACCOUNT

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData {
        val accountResult = accountGateway.getContext(session.telegramId)
        val balanceResult = loadBalance(session.telegramId, accountResult)

        return TelegramMenuScreenData.Account(
            AccountMenuScreenData(
                result = accountResult,
                balance = balanceResult,
                feedback = AccountMenuContext.feedback(session.screenContext),
                inputMode = inputMode(session.pendingInput),
            ),
        )
    }

    private fun loadBalance(
        telegramId: Long,
        result: TelegramAccountContextResult,
    ): TelegramAccountBalanceResult {
        val activeAccountId =
            (result as? TelegramAccountContextResult.Available)
                ?.context
                ?.activeAccountId

        if (activeAccountId == null) {
            return TelegramAccountBalanceResult.NotRequired
        }

        return balanceGateway.getBalances(telegramId)
    }

    private fun inputMode(pendingInput: TelegramPendingInput?): TelegramAccountInputMode? =
        when (pendingInput) {
            TelegramPendingInput.ACCOUNT_CREATE -> TelegramAccountInputMode.CREATE
            TelegramPendingInput.ACCOUNT_JOIN -> TelegramAccountInputMode.JOIN
            else -> null
        }
}
