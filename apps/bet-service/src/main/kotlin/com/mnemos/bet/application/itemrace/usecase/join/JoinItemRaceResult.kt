package com.mnemos.bet.application.itemrace.usecase.join

import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperationId

data class JoinItemRaceResult(
    val participantId: ItemRaceParticipantId,
    val operationId: WalletOperationId,
)
