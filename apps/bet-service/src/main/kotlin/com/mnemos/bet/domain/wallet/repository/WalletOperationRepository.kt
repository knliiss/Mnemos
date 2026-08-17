package com.mnemos.bet.domain.wallet.repository

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationId
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletRequestId
import com.mnemos.common.core.model.id.AccountId
import java.util.UUID

interface WalletOperationRepository {
    fun create(operation: WalletOperation): WalletOperation

    fun save(operation: WalletOperation): WalletOperation

    fun findById(operationId: WalletOperationId): WalletOperation?

    fun findByIdempotencyKey(idempotencyKey: UUID): WalletOperation?

    fun findByIdForUpdate(operationId: WalletOperationId): WalletOperation?

    fun findByRequestIdForUpdate(requestId: WalletRequestId): WalletOperation?

    fun findPendingByAccountId(accountId: AccountId): List<WalletOperation>

    fun findFailedCredits(): List<WalletOperation>

    fun existsForEventBet(
        eventBetId: EventBetId,
        kind: WalletOperationKind,
    ): Boolean

    fun existsForRaceParticipant(
        participantId: ItemRaceParticipantId,
        kind: WalletOperationKind,
    ): Boolean
}
