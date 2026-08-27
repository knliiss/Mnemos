package com.mnemos.payment.adapter.out.persistence.stars

import com.mnemos.payment.domain.stars.model.StarPrimePurchaseStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "star_prime_purchases")
class StarPrimePurchaseEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "telegram_id", nullable = false)
    var telegramId: Long,

    @Column(name = "package_id", nullable = false, length = 64)
    var packageId: String,

    @Column(name = "prime_amount", nullable = false)
    var primeAmount: Long,

    @Column(name = "stars_amount", nullable = false)
    var starsAmount: Int,

    @Column(name = "invoice_payload", nullable = false, unique = true, length = 128)
    var invoicePayload: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: StarPrimePurchaseStatus,

    @Column(name = "telegram_payment_charge_id", unique = true, length = 255)
    var telegramPaymentChargeId: String?,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "pre_checkout_approved_at")
    var preCheckoutApprovedAt: Instant?,

    @Column(name = "paid_at")
    var paidAt: Instant?,

    @Column(name = "fulfilled_at")
    var fulfilledAt: Instant?,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
