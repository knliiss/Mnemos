package com.mnemos.telegram.bot.adapter.out.payment

import com.mnemos.payment.contracts.internal.telegram.CreateTelegramStarPrimePurchaseRequest
import com.mnemos.payment.contracts.internal.telegram.CreatedTelegramStarPrimePurchaseResponse
import com.mnemos.payment.contracts.internal.telegram.RecordSuccessfulStarPaymentRequest
import com.mnemos.payment.contracts.internal.telegram.TelegramStarPrimePackageResponse
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutResponse
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseCreationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class PrimePurchaseGatewayAdapterTest {
    @Test
    fun `gateway maps PRIME catalog`() {
        val client =
            FakePaymentServiceClient(
                catalog =
                    listOf(
                        TelegramStarPrimePackageResponse(
                            id = "prime-100",
                            primeAmount = 100,
                            starsAmount = 100,
                        ),
                    ),
            )
        val gateway = PrimePurchaseGatewayAdapter(client)

        val catalog = requireNotNull(gateway.getCatalog())

        assertEquals(1, catalog.size)
        assertEquals("prime-100", catalog.single().id)
        assertEquals(100, catalog.single().primeAmount)
        assertEquals(100, catalog.single().starsAmount)
    }

    @Test
    fun `gateway forwards callback idempotency key and maps created invoice`() {
        val client =
            FakePaymentServiceClient(
                purchase =
                    CreatedTelegramStarPrimePurchaseResponse(
                        purchaseId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
                        packageId = "prime-250",
                        primeAmount = 250,
                        starsAmount = 250,
                        invoiceUrl = "https://t.me/invoice/example",
                    ),
            )
        val gateway = PrimePurchaseGatewayAdapter(client)

        val result =
            gateway.createPurchase(
                telegramId = 931458258L,
                packageId = "prime-250",
                requestId = "callback-query-42",
            )
        val invoice = (result as PrimePurchaseCreationResult.Ready).invoice

        assertEquals(
            CreateTelegramStarPrimePurchaseRequest(
                telegramId = 931458258L,
                packageId = "prime-250",
                requestId = "callback-query-42",
            ),
            client.lastPurchaseRequest,
        )
        assertEquals(250, invoice.primeAmount)
        assertEquals(250, invoice.starsAmount)
        assertEquals("https://t.me/invoice/example", invoice.url)
    }

    private class FakePaymentServiceClient(
        private val catalog: List<TelegramStarPrimePackageResponse> = emptyList(),
        private val purchase: CreatedTelegramStarPrimePurchaseResponse? = null,
    ) : PaymentServiceClient {
        var lastPurchaseRequest: CreateTelegramStarPrimePurchaseRequest? = null
            private set

        override fun getStarPrimeCatalog(): List<TelegramStarPrimePackageResponse> = catalog

        override fun createStarPrimePurchase(request: CreateTelegramStarPrimePurchaseRequest): CreatedTelegramStarPrimePurchaseResponse {
            lastPurchaseRequest = request

            return requireNotNull(purchase)
        }

        override fun validateStarPreCheckout(request: ValidateStarPreCheckoutRequest): ValidateStarPreCheckoutResponse = error("not used")

        override fun recordSuccessfulStarPayment(request: RecordSuccessfulStarPaymentRequest) {
            error("not used")
        }
    }
}
