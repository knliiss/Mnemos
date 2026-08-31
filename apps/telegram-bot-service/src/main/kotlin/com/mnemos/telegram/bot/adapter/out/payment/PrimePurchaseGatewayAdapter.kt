package com.mnemos.telegram.bot.adapter.out.payment

import com.mnemos.payment.contracts.internal.telegram.CreateTelegramStarPrimePurchaseRequest
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseCreationResult
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseInvoice
import com.mnemos.telegram.bot.application.prime.model.PrimePurchasePackage
import com.mnemos.telegram.bot.application.prime.port.out.PrimePurchaseGateway
import feign.FeignException
import org.springframework.stereotype.Component

@Component
class PrimePurchaseGatewayAdapter(
    private val client: PaymentServiceClient,
) : PrimePurchaseGateway {
    override fun getCatalog(): List<PrimePurchasePackage>? =
        try {
            client.getStarPrimeCatalog().map { primePackage ->
                PrimePurchasePackage(
                    id = primePackage.id,
                    primeAmount = primePackage.primeAmount,
                    starsAmount = primePackage.starsAmount,
                )
            }
        } catch (_: RuntimeException) {
            null
        }

    override fun createPurchase(
        telegramId: Long,
        packageId: String,
        requestId: String,
    ): PrimePurchaseCreationResult =
        try {
            val purchase =
                client.createStarPrimePurchase(
                    CreateTelegramStarPrimePurchaseRequest(
                        telegramId = telegramId,
                        packageId = packageId,
                        requestId = requestId,
                    ),
                )

            PrimePurchaseCreationResult.Ready(
                PrimePurchaseInvoice(
                    primeAmount = purchase.primeAmount,
                    starsAmount = purchase.starsAmount,
                    url = purchase.invoiceUrl,
                ),
            )
        } catch (exception: FeignException) {
            when (exception.status()) {
                404 -> PrimePurchaseCreationResult.RegistrationRequired
                in 400..499 -> PrimePurchaseCreationResult.Refresh
                else -> PrimePurchaseCreationResult.Unavailable
            }
        } catch (_: RuntimeException) {
            PrimePurchaseCreationResult.Unavailable
        }
}
