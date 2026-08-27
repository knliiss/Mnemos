package com.mnemos.payment.application.stars

import com.mnemos.common.web.exception.AppException
import com.mnemos.payment.application.stars.port.out.StarPrimeWallet
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RecordSuccessfulStarPaymentService(
    private val repository: StarPrimePurchaseRepository,
    private val wallet: StarPrimeWallet,
    private val clock: Clock,
) {
    @Transactional
    fun execute(
        telegramId: Long,
        invoicePayload: String,
        currency: String,
        totalAmount: Int,
        telegramPaymentChargeId: String,
    ) {
        val purchase =
            repository.findByInvoicePayload(invoicePayload)
                ?: throw paymentMismatch("Telegram payment references an unknown Mnemos invoice")

        if (purchase.telegramId != telegramId) {
            throw paymentMismatch("Telegram payment user does not match the Mnemos purchase")
        }
        if (currency != STARS_CURRENCY || totalAmount != purchase.starsAmount) {
            throw paymentMismatch("Telegram payment amount does not match the Mnemos purchase")
        }

        val chargeOwner = repository.findByTelegramPaymentChargeId(telegramPaymentChargeId)

        if (chargeOwner != null && chargeOwner.id != purchase.id) {
            throw paymentMismatch("Telegram payment charge is already assigned to another purchase")
        }

        val newlyPaid =
            try {
                purchase.recordSuccessfulPayment(
                    chargeId = telegramPaymentChargeId,
                    now = clock.instant(),
                )
            } catch (exception: IllegalArgumentException) {
                throw paymentMismatch(
                    message = "Telegram payment conflicts with the current Mnemos purchase state",
                    cause = exception,
                )
            } catch (exception: IllegalStateException) {
                throw paymentMismatch(
                    message = "Telegram payment conflicts with the current Mnemos purchase state",
                    cause = exception,
                )
            }

        repository.save(purchase)

        if (newlyPaid) {
            wallet.requestCredit(purchase)
        }
    }

    private fun paymentMismatch(
        message: String,
        cause: Throwable? = null,
    ): AppException =
        AppException(
            message = message,
            code = "TELEGRAM_PAYMENT_MISMATCH",
            httpStatus = HttpStatus.CONFLICT,
            cause = cause,
        )

    private companion object {
        const val STARS_CURRENCY = "XTR"
    }
}
