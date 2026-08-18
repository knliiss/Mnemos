package com.mnemos.moderation.adapter.out.persistence.wallet

import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentStatus
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "wallet_adjustment_operations")
class WalletAdjustmentOperationEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "request_id", nullable = false, unique = true, updatable = false)
    var requestId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, updatable = false)
    var targetType: WalletOwnerType,
    @Column(name = "target_id", nullable = false, updatable = false)
    var targetId: UUID,
    @Column(name = "actor_user_id", nullable = false, updatable = false)
    var actorUserId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, updatable = false)
    var currency: Currency,
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, updatable = false)
    var type: TransactionType,
    @Column(name = "amount", nullable = false, updatable = false)
    var amount: Long,
    @Column(name = "reason", nullable = false, length = 256, updatable = false)
    var reason: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: WalletAdjustmentStatus,
    @Column(name = "wallet_transaction_id")
    var walletTransactionId: UUID?,
    @Column(name = "failure_reason", length = 128)
    var failureReason: String?,
    @Column(name = "idempotency_key", unique = true, updatable = false)
    var idempotencyKey: UUID? = null,
)
