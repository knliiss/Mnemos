package com.mnemos.personalization.application.revenue.usecase.schedule

import com.mnemos.personalization.application.revenue.CreatorRevenuePolicy
import com.mnemos.personalization.application.revenue.port.out.CreatorRevenueWallet
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ScheduleCreatorRevenueService(
    private val purchaseOperationRepository: PurchaseOperationRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val revenueOperationRepository: CreatorRevenueOperationRepository,
    private val revenuePolicy: CreatorRevenuePolicy,
    private val wallet: CreatorRevenueWallet,
    private val clock: Clock,
) : ScheduleCreatorRevenueUseCase {
    @Transactional
    override fun execute(purchaseOperationId: PurchaseOperationId) {
        val purchase = purchaseOperationRepository.findByIdForUpdate(purchaseOperationId) ?: return

        if (purchase.status != PurchaseOperationStatus.CONFIRMED) {
            return
        }

        if (revenueOperationRepository.existsByPurchaseOperationId(purchaseOperationId)) {
            return
        }

        val personalization =
            requireNotNull(personalizationRepository.findById(purchase.personalizationId)) {
                "Purchased personalization ${purchase.personalizationId} no longer exists"
            }
        val creatorUserId = personalization.creatorUserId ?: return

        if (creatorUserId == purchase.buyerUserId) {
            return
        }

        val operation =
            CreatorRevenueOperation.create(
                purchaseOperationId = purchase.id,
                creatorUserId = creatorUserId,
                amountPrime = revenuePolicy.calculatePrime(purchase),
                now = clock.instant(),
            )

        revenueOperationRepository.save(operation)
        wallet.requestCredit(operation)
    }
}
