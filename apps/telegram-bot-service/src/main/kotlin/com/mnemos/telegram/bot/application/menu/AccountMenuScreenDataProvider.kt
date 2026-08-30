package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.account.AccountMenuContext
import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import org.springframework.stereotype.Component

@Component
class AccountMenuScreenDataProvider(
    private val accountGateway: AccountGateway,
    private val balanceGateway: AccountBalanceGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ACCOUNT

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData {
        val accountResult = accountGateway.getContext(session.telegramId)
        val balanceResult = loadBalance(accountResult)

        return TelegramMenuScreenData.Account(
            AccountMenuScreenData(
                result = accountResult,
                balance = balanceResult,
                feedback = AccountMenuContext.feedback(session.screenContext),
            ),
        )
    }

    private fun loadBalance(result: TelegramAccountContextResult): TelegramAccountBalanceResult {
        val accountId =
            (result as? TelegramAccountContextResult.Available)
                ?.context
                ?.activeAccountId
                ?: return TelegramAccountBalanceResult.NotRequired

        return balanceGateway.getBalances(accountId)
    }
}
