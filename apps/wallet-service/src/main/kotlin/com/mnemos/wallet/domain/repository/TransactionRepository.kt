package com.mnemos.wallet.domain.repository

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.api.dto.TransactionSummaryResponse
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.Transaction
import org.springframework.data.domain.Pageable

interface TransactionRepository {
    fun findByRequestId(requestId: RequestId): Transaction?

    fun create(transaction: Transaction)

    fun findByAccountId(
        accountId: AccountId,
        page: Pageable,
    ): List<TransactionSummaryResponse>
}
