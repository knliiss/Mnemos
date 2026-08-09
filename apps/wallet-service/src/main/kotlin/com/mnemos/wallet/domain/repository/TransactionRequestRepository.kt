package com.mnemos.wallet.domain.repository

import com.mnemos.wallet.domain.model.request.TransactionRequest
import com.mnemos.wallet.domain.model.request.TransactionRequestStatus
import com.mnemos.wallet.domain.model.transaction.RequestId

interface TransactionRequestRepository {
    fun createIfAbsent(request: TransactionRequest): Boolean

    fun findByRequestIdForUpdate(requestId: RequestId): TransactionRequest?

    fun update(request: TransactionRequest)

    fun findStatusByRequestId(requestId: RequestId): TransactionRequestStatus?
}
