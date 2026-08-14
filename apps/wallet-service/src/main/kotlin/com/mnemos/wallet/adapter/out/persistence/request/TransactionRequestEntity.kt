package com.mnemos.wallet.adapter.out.persistence.request

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.transaction.model.TransactionRequestStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "transaction_requests")
class TransactionRequestEntity(
    @Id
    @Column(name = "request_id", nullable = false, updatable = false)
    var requestId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, updatable = false, length = 16)
    var ownerType: WalletOwnerType,
    @Column(name = "owner_id", nullable = false, updatable = false)
    var ownerId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, updatable = false, length = 16)
    var currency: Currency,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 16)
    var type: TransactionType,
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, updatable = false, length = 64)
    var source: TransactionSource,
    @Column(name = "source_reference_id", nullable = false, updatable = false)
    var sourceReferenceId: UUID,
    @Column(name = "amount", nullable = false, updatable = false)
    var amount: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: TransactionRequestStatus,
    @Column(name = "transaction_id")
    var transactionId: UUID?,
    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 64)
    var failureReason: WalletTransactionFailureReason?,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Column(name = "completed_at")
    var completedAt: Instant?,
)
