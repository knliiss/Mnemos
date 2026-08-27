package com.mnemos.payment.contracts.internal.telegram

data class ValidateStarPreCheckoutResponse(
    val approved: Boolean,
    val errorMessage: String?,
)
