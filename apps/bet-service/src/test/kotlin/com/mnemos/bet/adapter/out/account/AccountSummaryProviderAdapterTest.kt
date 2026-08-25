package com.mnemos.bet.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.internal.v1.InternalAccountSummariesRequest
import com.mnemos.contracts.account.internal.v1.InternalAccountSummaryResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AccountSummaryProviderAdapterTest {
    private val client = mock<AccountInternalClient>()
    private val adapter = AccountSummaryProviderAdapter(client)

    @Test
    fun `summary lookup respects account service batch limit`() {
        val accountIds = (1..205).mapTo(linkedSetOf()) { AccountId.generate() }
        val requests = mutableListOf<InternalAccountSummariesRequest>()

        whenever(client.summaries(any())).thenAnswer { invocation ->
            val request = invocation.getArgument<InternalAccountSummariesRequest>(0)
            requests += request

            request.accountIds.map { accountId ->
                InternalAccountSummaryResponse(
                    accountId = accountId,
                    cristalixUuid = accountId,
                    cristalixNickname = accountId.toString(),
                )
            }
        }

        val result = adapter.getByIds(accountIds)

        assertEquals(205, result.size)
        assertEquals(listOf(100, 100, 5), requests.map { request -> request.accountIds.size })
        assertEquals(accountIds, result.keys)
    }
}
