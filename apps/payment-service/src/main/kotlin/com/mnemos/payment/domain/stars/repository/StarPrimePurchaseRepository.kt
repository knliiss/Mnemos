package com.mnemos.payment.domain.stars.repository

import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import java.util.UUID

interface StarPrimePurchaseRepository {
    fun save(purchase: StarPrimePurchase): StarPrimePurchase

    fun saveAndFlush(purchase: StarPrimePurchase): StarPrimePurchase

    fun findById(id: UUID): StarPrimePurchase?

    fun findByInvoicePayload(invoicePayload: String): StarPrimePurchase?

    fun findByTelegramPaymentChargeId(chargeId: String): StarPrimePurchase?

    fun findByTelegramRequestId(requestId: String): StarPrimePurchase?
}
