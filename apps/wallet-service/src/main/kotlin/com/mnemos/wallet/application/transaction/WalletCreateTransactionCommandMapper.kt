package com.mnemos.wallet.application.transaction

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.TransactionSourceReferenceId

object WalletCreateTransactionCommandMapper {
    fun WalletCreateTransactionCommand.toRequest(): CreateTransactionRequest =
        CreateTransactionRequest(
            requestId = RequestId(requestId),
            type = type,
            amount = amount,
            accountId = AccountId(accountId),
            currency = currency,
            source = source,
            sourceReferenceId = TransactionSourceReferenceId(sourceReferenceId),
        )
}
