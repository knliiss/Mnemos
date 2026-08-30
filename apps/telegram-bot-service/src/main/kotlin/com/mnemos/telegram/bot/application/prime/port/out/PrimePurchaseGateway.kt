package com.mnemos.telegram.bot.application.prime.port.out

import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseCreationResult
import com.mnemos.telegram.bot.application.prime.model.PrimePurchasePackage

interface PrimePurchaseGateway {
    fun getCatalog(): List<PrimePurchasePackage>?

    fun createPurchase(
        telegramId: Long,
        packageId: String,
    ): PrimePurchaseCreationResult
}
