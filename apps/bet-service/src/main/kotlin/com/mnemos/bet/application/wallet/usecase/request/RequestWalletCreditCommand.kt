package com.mnemos.bet.application.wallet.usecase.request

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetResolutionId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

data class RequestWalletCreditCommand(
    val accountId: AccountId,
    val amount: Long,
    val kind: WalletOperationKind,
    val eventBetId: EventBetId?,
    val eventBetResolutionId: EventBetResolutionId?,
    val raceParticipantId: ItemRaceParticipantId?,
    val eligibleAt: Instant,
)
