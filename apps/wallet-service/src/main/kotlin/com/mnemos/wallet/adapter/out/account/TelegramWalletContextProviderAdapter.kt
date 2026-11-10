package com.mnemos.wallet.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.application.account.model.TelegramWalletContext
import com.mnemos.wallet.application.account.port.out.TelegramWalletContextProvider
import org.springframework.stereotype.Component

@Component
class TelegramWalletContextProviderAdapter(
    private val client: AccountInternalClient,
) : TelegramWalletContextProvider {
    override fun findByTelegramId(telegramId: Long): TelegramWalletContext? =
        client.findTelegramWalletContext(telegramId)?.let { context ->
            TelegramWalletContext(
                userId = UserId(context.userId),
                activeAccountId = context.activeAccountId?.let(::AccountId),
            )
        }
}
