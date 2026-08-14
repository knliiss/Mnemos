package com.mnemos.wallet.application.transaction.mapper

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionRequest
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner as WalletOwnerContract

object WalletCreateTransactionCommandMapper {
    fun WalletCreateTransactionCommand.toRequest(): CreateTransactionRequest =
        CreateTransactionRequest(
            requestId = RequestId(requestId),
            owner = owner.toDomain(),
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = TransactionSourceReferenceId(sourceReferenceId),
            amount = amount,
        )

    private fun WalletOwnerContract.toDomain(): WalletOwner =
        when (type) {
            WalletOwnerType.ACCOUNT -> WalletOwner.Account(AccountId(id))
            WalletOwnerType.USER -> WalletOwner.User(UserId(id))
        }
}
