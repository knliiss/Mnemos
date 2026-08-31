package com.mnemos.payment.application.stars

import com.mnemos.payment.application.account.port.out.PaymentTelegramUserResolver
import com.mnemos.payment.application.security.port.out.CurrentPaymentUser
import com.mnemos.payment.application.stars.model.CreatedStarPrimePurchase
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.model.StarPrimePurchaseStatus
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class CreateTelegramStarPrimePurchaseServiceTest {
    private val telegramUserResolver = mock<PaymentTelegramUserResolver>()
    private val createPurchaseService = mock<CreateStarPrimePurchaseService>()
    private val repository = mock<StarPrimePurchaseRepository>()
    private val service =
        CreateTelegramStarPrimePurchaseService(
            telegramUserResolver = telegramUserResolver,
            createPurchaseService = createPurchaseService,
            repository = repository,
        )

    @Test
    fun `repeated Telegram callback returns stored invoice without creating another purchase`() {
        val purchase = existingPurchase()

        whenever(repository.findByTelegramRequestId(REQUEST_ID)).thenReturn(purchase)

        val result =
            service.execute(
                telegramId = TELEGRAM_ID,
                packageId = PACKAGE_ID,
                requestId = REQUEST_ID,
            )

        assertEquals(PURCHASE_ID, result.purchaseId)
        assertEquals(INVOICE_URL, result.invoiceUrl)
        verifyNoInteractions(telegramUserResolver, createPurchaseService)
    }

    @Test
    fun `new Telegram callback forwards request id into durable purchase creation`() {
        val user =
            CurrentPaymentUser(
                userId = USER_ID,
                telegramId = TELEGRAM_ID,
            )
        val createdPurchase =
            CreatedStarPrimePurchase(
                purchaseId = PURCHASE_ID,
                packageId = PACKAGE_ID,
                primeAmount = 250,
                starsAmount = 250,
                status = StarPrimePurchaseStatus.INVOICE_READY,
                invoiceUrl = INVOICE_URL,
            )

        whenever(repository.findByTelegramRequestId(REQUEST_ID)).thenReturn(null)
        whenever(telegramUserResolver.findByTelegramId(TELEGRAM_ID)).thenReturn(user)
        whenever(
            createPurchaseService.execute(
                packageId = PACKAGE_ID,
                user = user,
                telegramRequestId = REQUEST_ID,
            ),
        ).thenReturn(createdPurchase)

        val result =
            service.execute(
                telegramId = TELEGRAM_ID,
                packageId = PACKAGE_ID,
                requestId = REQUEST_ID,
            )

        assertEquals(createdPurchase, result)
        verify(createPurchaseService).execute(
            packageId = PACKAGE_ID,
            user = user,
            telegramRequestId = REQUEST_ID,
        )
    }

    private fun existingPurchase(): StarPrimePurchase {
        val purchase =
            StarPrimePurchase.create(
                id = PURCHASE_ID,
                userId = USER_ID,
                telegramId = TELEGRAM_ID,
                packageId = PACKAGE_ID,
                primeAmount = 250,
                starsAmount = 250,
                invoicePayload = "mnemos-prime:test-payload",
                createdAt = NOW,
                expiresAt = NOW.plusSeconds(900),
                telegramRequestId = REQUEST_ID,
            )

        purchase.markInvoiceReady(
            now = NOW.plusSeconds(1),
            invoiceUrl = INVOICE_URL,
        )

        return purchase
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-31T01:00:00Z")
        val PURCHASE_ID: UUID = UUID.fromString("019c113f-b116-7000-8000-000000000201")
        val USER_ID: UUID = UUID.fromString("019c113f-b116-7000-8000-000000000202")
        const val TELEGRAM_ID = 931458258L
        const val PACKAGE_ID = "prime-250"
        const val REQUEST_ID = "callback-query-42"
        const val INVOICE_URL = "https://t.me/invoice/example"
    }
}
