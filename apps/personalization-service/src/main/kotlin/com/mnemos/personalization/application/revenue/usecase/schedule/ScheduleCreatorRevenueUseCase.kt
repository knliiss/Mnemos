package com.mnemos.personalization.application.revenue.usecase.schedule

import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId

interface ScheduleCreatorRevenueUseCase {
    fun execute(purchaseOperationId: PurchaseOperationId)
}
