package com.mnemos.personalization.application.revenue.port.out

import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation

fun interface CreatorRevenueWallet {
    fun requestCredit(operation: CreatorRevenueOperation)
}
