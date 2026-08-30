package com.mnemos.payment.application.stars

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.web.exception.AppException
import com.mnemos.payment.application.security.port.out.CurrentPaymentUser
import com.mnemos.payment.application.security.port.out.CurrentPaymentUserProvider
import com.mnemos.payment.application.stars.model.CreatedStarPrimePurchase
import com.mnemos.payment.application.stars.port.out.TelegramStarInvoice
import com.mnemos.payment.application.stars.port.out.TelegramStarInvoiceGateway
import com.mnemos.payment.configuration.StarPrimePurchaseProperties
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class CreateStarPrimePurchaseService(
    private val currentUserProvider: CurrentPaymentUserProvider,
    private val packageCatalog: StarPrimePackageCatalog,
    private val repository: StarPrimePurchaseRepository,
    private val invoiceGateway: TelegramStarInvoiceGateway,
    private val properties: StarPrimePurchaseProperties,
    private val clock: Clock,
) {
    @Transactional
    fun execute(packageId: String): CreatedStarPrimePurchase =
        execute(
            packageId = packageId,
            user = currentUserProvider.requireCurrent(),
        )

    @Transactional
    fun execute(
        packageId: String,
        user: CurrentPaymentUser,
    ): CreatedStarPrimePurchase {
        val selectedPackage =
            try {
                packageCatalog.requireById(packageId)
            } catch (_: IllegalArgumentException) {
                throw AppException(
                    message = "Unknown PRIME purchase package",
                    code = "PRIME_PURCHASE_PACKAGE_INVALID",
                    httpStatus = HttpStatus.BAD_REQUEST,
                )
            }

        val now = clock.instant()
        val purchaseId = UuidCreator.getTimeOrderedEpoch()
        val invoicePayload = invoicePayload(purchaseId)
        val purchase =
            StarPrimePurchase.create(
                id = purchaseId,
                userId = user.userId,
                telegramId = user.telegramId,
                packageId = selectedPackage.id,
                primeAmount = selectedPackage.primeAmount,
                starsAmount = selectedPackage.starsAmount,
                invoicePayload = invoicePayload,
                createdAt = now,
                expiresAt = now.plus(properties.invoiceLifetime),
            )

        repository.save(purchase)

        val invoiceUrl =
            invoiceGateway.createInvoice(
                TelegramStarInvoice(
                    title = "Mnemos PRIME · ${selectedPackage.primeAmount}",
                    description = "${selectedPackage.primeAmount} PRIME for Mnemos",
                    payload = invoicePayload,
                    starsAmount = selectedPackage.starsAmount,
                    photoUrl = properties.invoicePhotoUrl?.trim()?.takeIf(String::isNotEmpty),
                ),
            )

        purchase.markInvoiceReady(clock.instant())
        repository.save(purchase)

        return CreatedStarPrimePurchase(
            purchaseId = purchase.id,
            packageId = purchase.packageId,
            primeAmount = purchase.primeAmount,
            starsAmount = purchase.starsAmount,
            status = purchase.status,
            invoiceUrl = invoiceUrl,
        )
    }

    private fun invoicePayload(purchaseId: UUID): String {
        val nonce = UUID.randomUUID().toString().replace("-", "")

        return "mnemos-prime:$purchaseId:$nonce"
    }
}
