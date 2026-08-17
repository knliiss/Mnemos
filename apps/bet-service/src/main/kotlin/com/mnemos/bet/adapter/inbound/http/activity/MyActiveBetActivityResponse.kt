package com.mnemos.bet.adapter.inbound.http.activity

import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetResponse
import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetRoundResponse
import com.mnemos.bet.adapter.inbound.http.itemrace.ItemRaceParticipantResponse
import com.mnemos.bet.adapter.inbound.http.itemrace.ItemRaceResponse
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import java.time.Instant
import java.util.UUID

data class MyActiveBetActivityResponse(
    val eventBets: List<ActiveEventBetResponse>,
    val itemRaces: List<ActiveItemRaceResponse>,
    val pendingOperations: List<PendingBetOperationResponse>,
)

data class ActiveEventBetResponse(
    val round: EventBetRoundResponse,
    val bet: EventBetResponse,
)

data class ActiveItemRaceResponse(
    val race: ItemRaceResponse,
    val participant: ItemRaceParticipantResponse,
)

data class PendingBetOperationResponse(
    val operationId: UUID,
    val kind: WalletOperationKind,
    val amount: Long,
    val eventBetId: UUID?,
    val itemRaceParticipantId: UUID?,
    val createdAt: Instant,
)
