package com.mnemos.payment.application.account.port.out

import com.mnemos.payment.application.security.port.out.CurrentPaymentUser

interface PaymentTelegramUserResolver {
    fun findByTelegramId(telegramId: Long): CurrentPaymentUser?

    fun getOrCreateByTelegramId(telegramId: Long): CurrentPaymentUser
}
