package com.mnemos.bet.application.itemrace.usecase.increase

import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperationId

data class IncreaseItemRaceBetResult(
    val participantId: ItemRaceParticipantId,
    val operationId: WalletOperationId,
)
