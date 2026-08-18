package com.mnemos.wallet.application.transaction.mapper

import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionResponse
import com.mnemos.wallet.domain.transaction.model.Transaction
import com.mnemos.wallet.domain.transaction.model.TransactionRequest

object CreateTransactionResponseMapper {
    fun Transaction.toResponse(): CreateTransactionResponse =
        CreateTransactionResponse(
            transactionId = id,
            requestId = requestId,
            owner = owner,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount.value,
        )

    fun TransactionRequest.toCompletedResponse(): CreateTransactionResponse =
        CreateTransactionResponse(
            transactionId = requireNotNull(transactionId),
            requestId = requestId,
            owner = owner,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount,
        )
}
