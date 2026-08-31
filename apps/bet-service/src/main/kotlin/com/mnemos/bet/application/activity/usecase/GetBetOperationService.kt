package com.mnemos.bet.application.activity.usecase

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationId
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetBetOperationService(
    private val accountProvider: AccountProvider,
    private val repository: WalletOperationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(operationId: WalletOperationId): WalletOperation =
        findForAccount(
            operationId = operationId,
            accountId = accountProvider.requireActiveAccount().id,
        )

    @Transactional(readOnly = true)
    fun executeFor(
        operationId: WalletOperationId,
        userId: UserId,
    ): WalletOperation =
        findForAccount(
            operationId = operationId,
            accountId = accountProvider.requireActiveAccount(userId).id,
        )

    private fun findForAccount(
        operationId: WalletOperationId,
        accountId: AccountId,
    ): WalletOperation {
        val operation = repository.findById(operationId)

        if (operation == null || operation.accountId != accountId) {
            throw BetNotFoundException("Bet operation was not found")
        }

        return operation
    }
}
