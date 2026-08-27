package com.mnemos.payment.application.stars

import com.mnemos.payment.application.stars.model.StarPreCheckoutDecision
import com.mnemos.payment.domain.stars.model.StarPrimePurchaseStatus
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ValidateStarPreCheckoutService(
    private val repository: StarPrimePurchaseRepository,
    private val clock: Clock,
) {
    @Transactional
    fun execute(
        telegramId: Long,
        invoicePayload: String,
        currency: String,
        totalAmount: Int,
    ): StarPreCheckoutDecision {
        val purchase =
            repository.findByInvoicePayload(invoicePayload)
                ?: return StarPreCheckoutDecision.rejected(
                    "Счёт не найден. Создайте новый счёт в Mnemos.",
                )
        val now = clock.instant()

        if (purchase.isExpired(now)) {
            return StarPreCheckoutDecision.rejected(
                "Срок действия счёта истёк. Создайте новый счёт в Mnemos.",
            )
        }

        if (purchase.telegramId != telegramId) {
            return StarPreCheckoutDecision.rejected(
                "Этот счёт создан для другого пользователя Telegram.",
            )
        }

        if (currency != STARS_CURRENCY || totalAmount != purchase.starsAmount) {
            return StarPreCheckoutDecision.rejected(
                "Параметры платежа изменились. Создайте новый счёт в Mnemos.",
            )
        }

        if (
            purchase.status != StarPrimePurchaseStatus.INVOICE_READY &&
            purchase.status != StarPrimePurchaseStatus.PRE_CHECKOUT_APPROVED
        ) {
            return StarPreCheckoutDecision.rejected(
                "Этот счёт уже обработан или больше недействителен.",
            )
        }

        purchase.approvePreCheckout(now)
        repository.save(purchase)

        return StarPreCheckoutDecision.approved()
    }

    private companion object {
        const val STARS_CURRENCY = "XTR"
    }
}
