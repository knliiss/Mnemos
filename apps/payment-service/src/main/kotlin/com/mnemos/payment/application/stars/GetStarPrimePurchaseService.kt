package com.mnemos.payment.application.stars

import com.mnemos.common.web.exception.AppException
import com.mnemos.payment.application.security.port.out.CurrentPaymentUserProvider
import com.mnemos.payment.application.stars.model.StarPrimePurchaseView
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetStarPrimePurchaseService(
    private val currentUserProvider: CurrentPaymentUserProvider,
    private val repository: StarPrimePurchaseRepository,
) {
    @Transactional(readOnly = true)
    fun execute(purchaseId: UUID): StarPrimePurchaseView {
        val user = currentUserProvider.requireCurrent()
        val purchase =
            repository.findById(purchaseId)
                ?: throw notFound(purchaseId)

        if (purchase.userId != user.userId) {
            throw notFound(purchaseId)
        }

        return StarPrimePurchaseView(
            purchaseId = purchase.id,
            packageId = purchase.packageId,
            primeAmount = purchase.primeAmount,
            starsAmount = purchase.starsAmount,
            status = purchase.status,
        )
    }

    private fun notFound(purchaseId: UUID): AppException =
        AppException(
            message = "PRIME purchase $purchaseId was not found",
            code = "PRIME_PURCHASE_NOT_FOUND",
            httpStatus = HttpStatus.NOT_FOUND,
        )
}
