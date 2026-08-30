package com.mnemos.payment.adapter.out.account

import com.mnemos.payment.application.account.port.out.PaymentTelegramUserResolver
import com.mnemos.payment.application.security.port.out.CurrentPaymentUser
import org.springframework.stereotype.Component

@Component
class PaymentTelegramUserResolverAdapter(
    private val client: PaymentAccountServiceClient,
) : PaymentTelegramUserResolver {
    override fun findByTelegramId(telegramId: Long): CurrentPaymentUser? =
        client
            .getTelegramUserContext(telegramId)
            ?.let { context ->
                CurrentPaymentUser(
                    userId = context.userId,
                    telegramId = context.telegramId,
                )
            }
}
