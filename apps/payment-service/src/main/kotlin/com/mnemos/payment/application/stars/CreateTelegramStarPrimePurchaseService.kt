package com.mnemos.payment.application.stars

import com.mnemos.common.web.exception.AppException
import com.mnemos.payment.application.account.port.out.PaymentTelegramUserResolver
import com.mnemos.payment.application.stars.model.CreatedStarPrimePurchase
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class CreateTelegramStarPrimePurchaseService(
    private val telegramUserResolver: PaymentTelegramUserResolver,
    private val createPurchaseService: CreateStarPrimePurchaseService,
    private val repository: StarPrimePurchaseRepository,
) {
    fun execute(
        telegramId: Long,
        packageId: String,
        requestId: String,
    ): CreatedStarPrimePurchase {
        val normalizedRequestId = requireRequestId(requestId)

        repository.findByTelegramRequestId(normalizedRequestId)?.let { existingPurchase ->
            return existingPurchase.toReplay(telegramId, packageId)
        }

        val user =
            telegramUserResolver.findByTelegramId(telegramId)
                ?: throw AppException(
                    message = "Telegram user is not registered in Mnemos",
                    code = "TELEGRAM_USER_NOT_REGISTERED",
                    httpStatus = HttpStatus.NOT_FOUND,
                )

        return try {
            createPurchaseService.execute(
                packageId = packageId,
                user = user,
                telegramRequestId = normalizedRequestId,
            )
        } catch (exception: DataIntegrityViolationException) {
            val existingPurchase =
                repository.findByTelegramRequestId(normalizedRequestId)
                    ?: throw exception

            existingPurchase.toReplay(telegramId, packageId)
        }
    }

    private fun requireRequestId(requestId: String): String {
        val normalized = requestId.trim()

        if (normalized.isEmpty() || normalized.length > MAX_REQUEST_ID_LENGTH) {
            throw AppException(
                message = "Telegram purchase request id is invalid",
                code = "TELEGRAM_PURCHASE_REQUEST_ID_INVALID",
                httpStatus = HttpStatus.BAD_REQUEST,
            )
        }

        return normalized
    }

    private fun StarPrimePurchase.toReplay(
        telegramId: Long,
        packageId: String,
    ): CreatedStarPrimePurchase {
        if (this.telegramId != telegramId || this.packageId != packageId) {
            throw AppException(
                message = "Telegram purchase request id is already used for another purchase",
                code = "TELEGRAM_PURCHASE_REQUEST_CONFLICT",
                httpStatus = HttpStatus.CONFLICT,
            )
        }

        val storedInvoiceUrl =
            invoiceUrl
                ?: throw AppException(
                    message = "Telegram purchase invoice is not ready",
                    code = "TELEGRAM_PURCHASE_INVOICE_NOT_READY",
                    httpStatus = HttpStatus.CONFLICT,
                )

        return CreatedStarPrimePurchase(
            purchaseId = id,
            packageId = this.packageId,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
            status = status,
            invoiceUrl = storedInvoiceUrl,
        )
    }

    private companion object {
        const val MAX_REQUEST_ID_LENGTH = 255
    }
}
