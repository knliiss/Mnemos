package com.mnemos.wallet.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.internal.v1.InternalAccountSummariesRequest
import com.mnemos.wallet.application.account.model.AccountSummary
import com.mnemos.wallet.application.account.port.out.AccountSummaryProvider
import org.springframework.stereotype.Component

@Component
class AccountSummaryProviderAdapter(
    private val client: AccountPermissionClient,
) : AccountSummaryProvider {
    override fun getByIds(accountIds: Set<AccountId>): Map<AccountId, AccountSummary> {
        if (accountIds.isEmpty()) {
            return emptyMap()
        }

        return client
            .summaries(
                InternalAccountSummariesRequest(
                    accountIds = accountIds.mapTo(linkedSetOf()) { it.id },
                ),
            ).associate { response ->
                val accountId = AccountId(response.accountId)

                accountId to
                    AccountSummary(
                        accountId = accountId,
                        cristalixUuid = response.cristalixUuid,
                        cristalixNickname = response.cristalixNickname,
                    )
            }
    }
}
