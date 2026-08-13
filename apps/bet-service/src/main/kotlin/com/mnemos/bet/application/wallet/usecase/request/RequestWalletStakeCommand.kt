package com.mnemos.bet.application.wallet.usecase.request

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

data class RequestWalletStakeCommand(
    val accountId: AccountId,
    val amount: Long,
    val eventBetId: EventBetId?,
    val raceParticipantId: ItemRaceParticipantId?,
    val eligibleAt: Instant,
)
