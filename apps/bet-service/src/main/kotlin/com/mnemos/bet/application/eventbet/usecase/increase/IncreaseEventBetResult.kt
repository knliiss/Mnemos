package com.mnemos.bet.application.eventbet.usecase.increase

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.wallet.model.WalletOperationId

data class IncreaseEventBetResult(
    val betId: EventBetId,
    val operationId: WalletOperationId,
)
