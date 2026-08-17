package com.mnemos.bet.application.wallet.usecase.request

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperationRequestType
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import java.time.Instant
import java.util.UUID

data class RequestWalletStakeCommand(
    val accountId: AccountId,
    val initiatedByUserId: UserId,
    val amount: Long,
    val eventBetId: EventBetId?,
    val raceParticipantId: ItemRaceParticipantId?,
    val eligibleAt: Instant,
    val requestType: WalletOperationRequestType? = null,
    val idempotencyKey: UUID? = null,
)
