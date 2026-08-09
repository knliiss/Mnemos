package com.mnemos.wallet.infrastructure.persistence.balance

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "balances")
class BalanceEntity(
    @EmbeddedId
    var id: BalanceEntityId,
    @Column(name = "amount", nullable = false)
    var amount: Long,
    @Column(name = "last_transaction_id")
    var lastTransactionId: UUID?,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
