package com.mnemos.payment.application.stars

import com.mnemos.payment.application.stars.port.out.StarPrimeWallet
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class RecordSuccessfulStarPaymentServiceTest {
    private val repository = mock<StarPrimePurchaseRepository>()
    private val wallet = mock<StarPrimeWallet>()
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val service =
        RecordSuccessfulStarPaymentService(
            repository = repository,
            wallet = wallet,
            clock = clock,
        )

    @Test
    fun `successful payment requests PRIME credit only once for duplicate Telegram delivery`() {
        val purchase = purchase()

        purchase.markInvoiceReady(NOW.minusSeconds(20))
        purchase.approvePreCheckout(NOW.minusSeconds(10))

        whenever(repository.findByInvoicePayload(purchase.invoicePayload)).thenReturn(purchase)
        whenever(repository.findByTelegramPaymentChargeId(CHARGE_ID)).thenReturn(null)

        service.execute(
            telegramId = TELEGRAM_ID,
            invoicePayload = purchase.invoicePayload,
            currency = "XTR",
            totalAmount = STARS_AMOUNT,
            telegramPaymentChargeId = CHARGE_ID,
        )
        service.execute(
            telegramId = TELEGRAM_ID,
            invoicePayload = purchase.invoicePayload,
            currency = "XTR",
            totalAmount = STARS_AMOUNT,
            telegramPaymentChargeId = CHARGE_ID,
        )

        verify(wallet, times(1)).requestCredit(purchase)
        assertEquals(CHARGE_ID, purchase.telegramPaymentChargeId)
        assertEquals(NOW, purchase.paidAt)
    }

    private fun purchase(): StarPrimePurchase =
        StarPrimePurchase.create(
            id = UUID.fromString("019c113f-b116-7000-8000-000000000201"),
            userId = UUID.fromString("019c113f-b116-7000-8000-000000000202"),
            telegramId = TELEGRAM_ID,
            packageId = "prime-25-stars-25",
            primeAmount = 25,
            starsAmount = STARS_AMOUNT,
            invoicePayload = "mnemos-prime:test-successful-payment",
            createdAt = NOW.minusSeconds(60),
            expiresAt = NOW.plusSeconds(600),
        )

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-27T20:00:00Z")
        const val TELEGRAM_ID = 931458258L
        const val STARS_AMOUNT = 25
        const val CHARGE_ID = "telegram-charge-001"
    }
}
