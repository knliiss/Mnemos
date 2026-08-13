package com.mnemos.bet.adapter.out.persistence.wallet

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetResolutionId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationId
import com.mnemos.bet.domain.wallet.model.WalletRequestId
import com.mnemos.bet.domain.wallet.model.WalletTransactionId
import com.mnemos.common.core.model.id.AccountId

object WalletOperationPersistenceMapper {
    fun WalletOperation.toEntity(): WalletOperationEntity =
        WalletOperationEntity(
            id = id.id,
            requestId = requestId.id,
            accountId = accountId.id,
            amount = amount,
            kind = kind,
            eventBetId = eventBetId?.id,
            eventBetResolutionId = eventBetResolutionId?.id,
            raceParticipantId = raceParticipantId?.id,
            createdAt = createdAt,
            eligibleAt = eligibleAt,
            status = status,
            walletTransactionId = walletTransactionId?.id,
        )

    fun WalletOperationEntity.toDomain(): WalletOperation =
        WalletOperation.restore(
            id = WalletOperationId(id),
            requestId = WalletRequestId(requestId),
            accountId = AccountId(accountId),
            amount = amount,
            kind = kind,
            eventBetId = eventBetId?.let(::EventBetId),
            eventBetResolutionId = eventBetResolutionId?.let(::EventBetResolutionId),
            raceParticipantId = raceParticipantId?.let(::ItemRaceParticipantId),
            createdAt = createdAt,
            eligibleAt = eligibleAt,
            status = status,
            walletTransactionId = walletTransactionId?.let(::WalletTransactionId),
        )
}
