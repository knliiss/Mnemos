package com.mnemos.payment.application.account.port.out

import com.mnemos.payment.application.security.port.out.CurrentPaymentUser

fun interface PaymentTelegramUserResolver {
    fun findByTelegramId(telegramId: Long): CurrentPaymentUser?
}
