package com.mnemos.payment.adapter.out.persistence.stars

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface StarPrimePurchaseJpaRepository : JpaRepository<StarPrimePurchaseEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT purchase FROM StarPrimePurchaseEntity purchase WHERE purchase.invoicePayload = :invoicePayload")
    fun findByInvoicePayloadForUpdate(
        @Param("invoicePayload") invoicePayload: String,
    ): StarPrimePurchaseEntity?

    fun findByTelegramPaymentChargeId(telegramPaymentChargeId: String): StarPrimePurchaseEntity?
}
