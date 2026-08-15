package com.mnemos.bet.application.eventbet.usecase.place

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.wallet.model.WalletOperationId

data class PlaceEventBetResult(
    val betId: EventBetId,
    val operationId: WalletOperationId,
)
