package com.mnemos.personalization.adapter.out.persistence.revenue

import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "creator_revenue_operations")
class CreatorRevenueOperationEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "request_id", nullable = false, unique = true)
    var requestId: UUID,
    @Column(name = "purchase_operation_id", nullable = false, unique = true)
    var purchaseOperationId: UUID,
    @Column(name = "creator_user_id", nullable = false)
    var creatorUserId: UUID,
    @Column(name = "amount_prime", nullable = false)
    var amountPrime: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: CreatorRevenueOperationStatus,
    @Column(name = "wallet_transaction_id")
    var walletTransactionId: UUID?,
    @Column(name = "failure_reason", length = 128)
    var failureReason: String?,
    @Column(name = "next_retry_at")
    var nextRetryAt: Instant?,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
