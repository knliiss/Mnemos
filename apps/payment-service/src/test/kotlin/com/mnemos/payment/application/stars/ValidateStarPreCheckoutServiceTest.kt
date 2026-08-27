package com.mnemos.payment.application.stars

import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ValidateStarPreCheckoutServiceTest {
    private val repository = mock<StarPrimePurchaseRepository>()
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val service =
        ValidateStarPreCheckoutService(
            repository = repository,
            clock = clock,
        )

    @Test
    fun `approves matching unexpired Telegram Stars pre-checkout`() {
        val purchase = purchase()

        purchase.markInvoiceReady(NOW.minusSeconds(1))
        whenever(repository.findByInvoicePayload(purchase.invoicePayload)).thenReturn(purchase)

        val result =
            service.execute(
                telegramId = TELEGRAM_ID,
                invoicePayload = purchase.invoicePayload,
                currency = "XTR",
                totalAmount = STARS_AMOUNT,
            )

        assertTrue(result.approved)
        assertTrue(result.errorMessage == null)
        verify(repository).save(purchase)
    }

    @Test
    fun `rejects pre-checkout created for another Telegram user`() {
        val purchase = purchase()

        purchase.markInvoiceReady(NOW.minusSeconds(1))
        whenever(repository.findByInvoicePayload(purchase.invoicePayload)).thenReturn(purchase)

        val result =
            service.execute(
                telegramId = TELEGRAM_ID + 1,
                invoicePayload = purchase.invoicePayload,
                currency = "XTR",
                totalAmount = STARS_AMOUNT,
            )

        assertFalse(result.approved)
    }

    @Test
    fun `rejects pre-checkout when Stars amount differs from stored purchase`() {
        val purchase = purchase()

        purchase.markInvoiceReady(NOW.minusSeconds(1))
        whenever(repository.findByInvoicePayload(purchase.invoicePayload)).thenReturn(purchase)

        val result =
            service.execute(
                telegramId = TELEGRAM_ID,
                invoicePayload = purchase.invoicePayload,
                currency = "XTR",
                totalAmount = STARS_AMOUNT + 1,
            )

        assertFalse(result.approved)
    }

    @Test
    fun `rejects expired pre-checkout`() {
        val purchase = purchase(expiresAt = NOW.minusSeconds(1))

        purchase.markInvoiceReady(NOW.minusSeconds(10))
        whenever(repository.findByInvoicePayload(purchase.invoicePayload)).thenReturn(purchase)

        val result =
            service.execute(
                telegramId = TELEGRAM_ID,
                invoicePayload = purchase.invoicePayload,
                currency = "XTR",
                totalAmount = STARS_AMOUNT,
            )

        assertFalse(result.approved)
    }

    private fun purchase(
        expiresAt: Instant = NOW.plusSeconds(600),
    ): StarPrimePurchase =
        StarPrimePurchase.create(
            id = UUID.fromString("019c113f-b116-7000-8000-000000000101"),
            userId = UUID.fromString("019c113f-b116-7000-8000-000000000102"),
            telegramId = TELEGRAM_ID,
            packageId = "prime-25-stars-25",
            primeAmount = 25,
            starsAmount = STARS_AMOUNT,
            invoicePayload = "mnemos-prime:test-payload",
            createdAt = NOW.minusSeconds(30),
            expiresAt = expiresAt,
        )

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-27T20:00:00Z")
        const val TELEGRAM_ID = 931458258L
        const val STARS_AMOUNT = 25
    }
}
