package com.mnemos.payment.application.stars.model

data class StarPreCheckoutDecision(
    val approved: Boolean,
    val errorMessage: String?,
) {
    companion object {
        fun approved(): StarPreCheckoutDecision =
            StarPreCheckoutDecision(
                approved = true,
                errorMessage = null,
            )

        fun rejected(message: String): StarPreCheckoutDecision =
            StarPreCheckoutDecision(
                approved = false,
                errorMessage = message,
            )
    }
}
