package com.mnemos.telegram.bot.application.prime.model

sealed interface PrimePurchaseCreationResult {
    data class Ready(
        val invoice: PrimePurchaseInvoice,
    ) : PrimePurchaseCreationResult

    data object RegistrationRequired : PrimePurchaseCreationResult

    data object Refresh : PrimePurchaseCreationResult

    data object Unavailable : PrimePurchaseCreationResult
}
