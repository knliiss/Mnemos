package com.mnemos.bet.adapter.out.persistence.wallet

import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "wallet_operations")
class WalletOperationEntity(
    @Id
    var id: UUID,
    @Column(name = "request_id", nullable = false, unique = true)
    var requestId: UUID,
    @Column(name = "account_id", nullable = false)
    var accountId: UUID,
    @Column(name = "amount", nullable = false)
    var amount: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    var kind: WalletOperationKind,
    @Column(name = "event_bet_id")
    var eventBetId: UUID?,
    @Column(name = "event_bet_resolution_id")
    var eventBetResolutionId: UUID?,
    @Column(name = "race_participant_id")
    var raceParticipantId: UUID?,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "eligible_at", nullable = false)
    var eligibleAt: Instant,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: WalletOperationStatus,
    @Column(name = "wallet_transaction_id")
    var walletTransactionId: UUID?,
)
