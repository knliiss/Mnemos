package com.mnemos.telegram.bot.adapter.out.wallet

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalance
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountBalanceGatewayAdapter(
    private val client: WalletServiceClient,
) : AccountBalanceGateway {
    override fun getBalances(accountId: UUID): TelegramAccountBalanceResult =
        try {
            val balances =
                client
                    .getAccountBalances(accountId)
                    .associate { balance -> balance.currency to balance.amount }

            TelegramAccountBalanceResult.Available(
                TelegramAccountBalance(
                    coin = balances[Currency.COIN] ?: 0,
                    prime = balances[Currency.PRIME] ?: 0,
                ),
            )
        } catch (_: RuntimeException) {
            TelegramAccountBalanceResult.Unavailable
        }
}
