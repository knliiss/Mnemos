package com.mnemos.personalization.application.purchase.port.out

import com.mnemos.personalization.domain.purchase.model.PurchaseOperation

interface PersonalizationWallet {
    fun requestDebit(operation: PurchaseOperation)
}
