package com.mnemos.wallet.adapter.out.persistence.transaction

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
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
@Table(name = "transactions")
class TransactionEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,
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
    @Column(name = "request_id", nullable = false, updatable = false)
    var requestId: UUID,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
)
