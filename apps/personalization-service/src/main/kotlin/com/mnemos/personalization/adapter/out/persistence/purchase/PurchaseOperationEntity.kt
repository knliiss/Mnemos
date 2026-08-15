package com.mnemos.personalization.adapter.out.persistence.purchase

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.wallet.contracts.wallet.shared.Currency
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "purchase_operations")
class PurchaseOperationEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "request_id", nullable = false, unique = true)
    var requestId: UUID,
    @Column(name = "personalization_id", nullable = false)
    var personalizationId: UUID,
    @Column(name = "buyer_user_id", nullable = false)
    var buyerUserId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 16)
    var ownerType: PersonalizationTarget,
    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 16)
    var currency: Currency,
    @Column(name = "amount", nullable = false)
    var amount: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: PurchaseOperationStatus,
    @Column(name = "wallet_transaction_id")
    var walletTransactionId: UUID?,
    @Column(name = "failure_reason", length = 128)
    var failureReason: String?,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
