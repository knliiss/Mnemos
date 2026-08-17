package com.mnemos.bet.application.activity.usecase

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationId
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.permission.RolePermission.VIEW_BETS
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class GetBetOperationService(
    private val accountProvider: AccountProvider,
    private val repository: WalletOperationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(operationId: WalletOperationId): WalletOperation {
        val account = accountProvider.requireActiveAccount(VIEW_BETS)
        val operation = repository.findById(operationId)

        if (operation == null || operation.accountId != account.id) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Bet operation was not found")
        }

        return operation
    }
}
