package com.mnemos.payment.application.stars

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.web.exception.AppException
import com.mnemos.payment.application.security.port.out.CurrentPaymentUser
import com.mnemos.payment.application.security.port.out.CurrentPaymentUserProvider
import com.mnemos.payment.application.stars.model.CreatedStarPrimePurchase
import com.mnemos.payment.application.stars.model.StarPrimePackage
import com.mnemos.payment.application.stars.port.out.TelegramStarInvoice
import com.mnemos.payment.application.stars.port.out.TelegramStarInvoiceGateway
import com.mnemos.payment.configuration.StarPrimePurchaseProperties
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
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
    private val transactionTemplate: TransactionTemplate,
) {
    fun execute(packageId: String): CreatedStarPrimePurchase =
        execute(
            packageId = packageId,
            user = currentUserProvider.requireCurrent(),
        )

    fun execute(
        packageId: String,
        user: CurrentPaymentUser,
        telegramRequestId: String? = null,
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

        val purchase =
            requireNotNull(
                transactionTemplate.execute {
                    createPurchase(
                        selectedPackage = selectedPackage,
                        user = user,
                        telegramRequestId = telegramRequestId,
                    )
                },
            ) {
                "PRIME purchase creation transaction returned no result"
            }

        val invoiceUrl =
            invoiceGateway.createInvoice(
                TelegramStarInvoice(
                    title = "Mnemos PRIME · ${purchase.primeAmount}",
                    description = "${purchase.primeAmount} PRIME for Mnemos",
                    payload = purchase.invoicePayload,
                    starsAmount = purchase.starsAmount,
                    photoUrl = properties.invoicePhotoUrl?.trim()?.takeIf(String::isNotEmpty),
                ),
            )

        return requireNotNull(
            transactionTemplate.execute {
                markInvoiceReady(
                    purchaseId = purchase.id,
                    invoiceUrl = invoiceUrl,
                )
            },
        ) {
            "PRIME invoice-ready transaction returned no result"
        }
    }

    private fun createPurchase(
        selectedPackage: StarPrimePackage,
        user: CurrentPaymentUser,
        telegramRequestId: String?,
    ): StarPrimePurchase {
        val now = clock.instant()
        val purchaseId = UuidCreator.getTimeOrderedEpoch()
        val purchase =
            StarPrimePurchase.create(
                id = purchaseId,
                userId = user.userId,
                telegramId = user.telegramId,
                packageId = selectedPackage.id,
                primeAmount = selectedPackage.primeAmount,
                starsAmount = selectedPackage.starsAmount,
                invoicePayload = invoicePayload(purchaseId),
                createdAt = now,
                expiresAt = now.plus(properties.invoiceLifetime),
                telegramRequestId = telegramRequestId,
            )

        return repository.saveAndFlush(purchase)
    }

    private fun markInvoiceReady(
        purchaseId: UUID,
        invoiceUrl: String,
    ): CreatedStarPrimePurchase {
        val purchase =
            checkNotNull(repository.findById(purchaseId)) {
                "PRIME purchase $purchaseId disappeared before invoice persistence"
            }

        purchase.markInvoiceReady(
            now = clock.instant(),
            invoiceUrl = invoiceUrl,
        )
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
