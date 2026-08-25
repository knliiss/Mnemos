package com.mnemos.bet.adapter.out.account

import com.mnemos.bet.application.account.model.AccountSummary
import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.internal.v1.InternalAccountSummariesRequest
import org.springframework.stereotype.Component

@Component
class AccountSummaryProviderAdapter(
    private val client: AccountInternalClient,
) : AccountSummaryProvider {
    override fun getByIds(accountIds: Set<AccountId>): Map<AccountId, AccountSummary> {
        if (accountIds.isEmpty()) {
            return emptyMap()
        }

        return accountIds
            .chunked(MAX_BATCH_SIZE)
            .flatMap { batch ->
                client.summaries(
                    InternalAccountSummariesRequest(
                        accountIds = batch.mapTo(linkedSetOf()) { it.id },
                    ),
                )
            }.associate { response ->
                val accountId = AccountId(response.accountId)

                accountId to
                    AccountSummary(
                        accountId = accountId,
                        nickname = response.cristalixNickname,
                    )
            }
    }

    private companion object {
        const val MAX_BATCH_SIZE = 100
    }
}
