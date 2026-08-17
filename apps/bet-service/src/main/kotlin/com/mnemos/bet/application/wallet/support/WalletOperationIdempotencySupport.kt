package com.mnemos.bet.application.wallet.support

import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationRequestType
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class WalletOperationIdempotencySupport(
    private val repository: WalletOperationRepository,
) {
    fun findStakeReplay(
        idempotencyKey: UUID,
        requestType: WalletOperationRequestType,
        accountId: AccountId,
        initiatedByUserId: UserId,
        amount: Long,
    ): WalletOperation? {
        val operation = repository.findByIdempotencyKey(idempotencyKey) ?: return null

        if (
            operation.kind != WalletOperationKind.STAKE ||
            operation.requestType != requestType ||
            operation.accountId != accountId ||
            operation.initiatedByUserId != initiatedByUserId ||
            operation.amount != amount
        ) {
            throw BetConflictException("Idempotency key is already used by another request")
        }

        return operation
    }
}
