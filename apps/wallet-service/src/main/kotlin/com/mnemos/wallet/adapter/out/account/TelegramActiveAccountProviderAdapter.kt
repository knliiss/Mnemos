package com.mnemos.wallet.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.account.port.out.TelegramActiveAccountProvider
import org.springframework.stereotype.Component

@Component
class TelegramActiveAccountProviderAdapter(
    private val client: AccountInternalClient,
) : TelegramActiveAccountProvider {
    override fun findActiveAccountId(telegramId: Long): AccountId? =
        client
            .resolveTelegramActiveAccount(telegramId)
            ?.activeAccountId
            ?.let(::AccountId)
}
