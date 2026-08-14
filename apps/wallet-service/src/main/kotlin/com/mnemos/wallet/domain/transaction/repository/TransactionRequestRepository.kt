package com.mnemos.wallet.domain.transaction.repository

import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.TransactionRequest

interface TransactionRequestRepository {
    fun createIfAbsent(request: TransactionRequest): Boolean

    fun findByRequestIdForUpdate(requestId: RequestId): TransactionRequest?

    fun update(request: TransactionRequest)
}
