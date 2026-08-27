package com.mnemos.payment.contracts.internal.telegram

data class CreateStarInvoiceRequest(
    val title: String,
    val description: String,
    val payload: String,
    val starsAmount: Int,
    val photoUrl: String?,
)
