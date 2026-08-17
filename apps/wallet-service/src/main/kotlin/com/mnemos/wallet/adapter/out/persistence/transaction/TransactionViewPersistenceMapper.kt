package com.mnemos.wallet.adapter.out.persistence.transaction

import com.mnemos.wallet.application.transaction.model.TransactionView

object TransactionViewPersistenceMapper {
    fun TransactionEntity.toView(): TransactionView =
        TransactionView(
            id = id,
            requestId = requestId,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId,
            amount = amount,
            createdAt = createdAt,
        )
}
