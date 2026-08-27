package com.mnemos.payment.application.security.port.out

import java.util.UUID

data class CurrentPaymentUser(
    val userId: UUID,
    val telegramId: Long,
)

fun interface CurrentPaymentUserProvider {
    fun requireCurrent(): CurrentPaymentUser
}
