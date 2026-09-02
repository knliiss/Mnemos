package com.mnemos.payment.adapter.out.persistence.stars

import com.mnemos.payment.domain.stars.model.StarPrimePurchase

object StarPrimePurchasePersistenceMapper {
    fun StarPrimePurchase.toEntity(): StarPrimePurchaseEntity =
        StarPrimePurchaseEntity(
            id = id,
            userId = userId,
            telegramId = telegramId,
            packageId = packageId,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
            invoicePayload = invoicePayload,
            telegramRequestId = telegramRequestId,
            invoiceUrl = invoiceUrl,
            status = status,
            telegramPaymentChargeId = telegramPaymentChargeId,
            createdAt = createdAt,
            expiresAt = expiresAt,
            preCheckoutApprovedAt = preCheckoutApprovedAt,
            paidAt = paidAt,
            fulfilledAt = fulfilledAt,
            updatedAt = updatedAt,
        )

    fun StarPrimePurchaseEntity.toDomain(): StarPrimePurchase =
        StarPrimePurchase(
            id = id,
            userId = userId,
            telegramId = telegramId,
            packageId = packageId,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
            invoicePayload = invoicePayload,
            createdAt = createdAt,
            expiresAt = expiresAt,
            telegramRequestId = telegramRequestId,
            invoiceUrl = invoiceUrl,
            status = status,
            telegramPaymentChargeId = telegramPaymentChargeId,
            preCheckoutApprovedAt = preCheckoutApprovedAt,
            paidAt = paidAt,
            fulfilledAt = fulfilledAt,
            updatedAt = updatedAt,
        )
}
