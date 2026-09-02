package com.mnemos.payment.domain.stars.model

import java.time.Instant
import java.util.UUID

class StarPrimePurchase(
    val id: UUID,
    val userId: UUID,
    val telegramId: Long,
    val packageId: String,
    val primeAmount: Long,
    val starsAmount: Int,
    val invoicePayload: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val telegramRequestId: String? = null,
    invoiceUrl: String? = null,
    status: StarPrimePurchaseStatus,
    telegramPaymentChargeId: String?,
    preCheckoutApprovedAt: Instant?,
    paidAt: Instant?,
    fulfilledAt: Instant?,
    updatedAt: Instant,
) {
    var invoiceUrl: String? = invoiceUrl
        private set

    var status: StarPrimePurchaseStatus = status
        private set

    var telegramPaymentChargeId: String? = telegramPaymentChargeId
        private set

    var preCheckoutApprovedAt: Instant? = preCheckoutApprovedAt
        private set

    var paidAt: Instant? = paidAt
        private set

    var fulfilledAt: Instant? = fulfilledAt
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun markInvoiceReady(
        now: Instant,
        invoiceUrl: String? = null,
    ) {
        require(status == StarPrimePurchaseStatus.CREATED) {
            "Purchase $id cannot become invoice-ready from $status"
        }

        this.invoiceUrl = invoiceUrl
        status = StarPrimePurchaseStatus.INVOICE_READY
        updatedAt = now
    }

    fun approvePreCheckout(now: Instant) {
        if (status == StarPrimePurchaseStatus.PRE_CHECKOUT_APPROVED) {
            return
        }

        require(status == StarPrimePurchaseStatus.INVOICE_READY) {
            "Purchase $id cannot approve pre-checkout from $status"
        }

        status = StarPrimePurchaseStatus.PRE_CHECKOUT_APPROVED
        preCheckoutApprovedAt = now
        updatedAt = now
    }

    fun recordSuccessfulPayment(
        chargeId: String,
        now: Instant,
    ): Boolean {
        if (
            status == StarPrimePurchaseStatus.PAID ||
            status == StarPrimePurchaseStatus.FULFILLED ||
            status == StarPrimePurchaseStatus.FULFILLMENT_FAILED
        ) {
            require(telegramPaymentChargeId == chargeId) {
                "Purchase $id is already bound to a different Telegram payment charge"
            }

            return false
        }

        require(
            status == StarPrimePurchaseStatus.PRE_CHECKOUT_APPROVED ||
                status == StarPrimePurchaseStatus.INVOICE_READY,
        ) {
            "Purchase $id cannot become paid from $status"
        }

        status = StarPrimePurchaseStatus.PAID
        telegramPaymentChargeId = chargeId
        paidAt = now
        updatedAt = now

        return true
    }

    fun markFulfilled(now: Instant) {
        if (status == StarPrimePurchaseStatus.FULFILLED) {
            return
        }

        require(
            status == StarPrimePurchaseStatus.PAID ||
                status == StarPrimePurchaseStatus.FULFILLMENT_FAILED,
        ) {
            "Purchase $id cannot become fulfilled from $status"
        }

        status = StarPrimePurchaseStatus.FULFILLED
        fulfilledAt = now
        updatedAt = now
    }

    fun markFulfillmentFailed(now: Instant) {
        if (status == StarPrimePurchaseStatus.FULFILLED) {
            return
        }

        require(
            status == StarPrimePurchaseStatus.PAID ||
                status == StarPrimePurchaseStatus.FULFILLMENT_FAILED,
        ) {
            "Purchase $id cannot fail fulfillment from $status"
        }

        status = StarPrimePurchaseStatus.FULFILLMENT_FAILED
        updatedAt = now
    }

    fun isExpired(now: Instant): Boolean = now.isAfter(expiresAt)

    companion object {
        fun create(
            id: UUID,
            userId: UUID,
            telegramId: Long,
            packageId: String,
            primeAmount: Long,
            starsAmount: Int,
            invoicePayload: String,
            createdAt: Instant,
            expiresAt: Instant,
            telegramRequestId: String? = null,
        ): StarPrimePurchase =
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
                invoiceUrl = null,
                status = StarPrimePurchaseStatus.CREATED,
                telegramPaymentChargeId = null,
                preCheckoutApprovedAt = null,
                paidAt = null,
                fulfilledAt = null,
                updatedAt = createdAt,
            )
    }
}
