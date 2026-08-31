package com.mnemos.payment.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import com.mnemos.payment.application.account.port.out.PaymentTelegramUserResolver
import com.mnemos.payment.application.security.port.out.CurrentPaymentUser
import org.springframework.stereotype.Component

@Component
class PaymentTelegramUserResolverAdapter(
    private val client: PaymentAccountServiceClient,
) : PaymentTelegramUserResolver {
    override fun findByTelegramId(telegramId: Long): CurrentPaymentUser? =
        client
            .getTelegramUserIdentity(telegramId)
            ?.toPaymentUser(telegramId)

    override fun getOrCreateByTelegramId(telegramId: Long): CurrentPaymentUser =
        client
            .bootstrapTelegramUserIdentity(telegramId)
            .toPaymentUser(telegramId)

    private fun InternalTelegramUserIdentityResponse.toPaymentUser(telegramId: Long): CurrentPaymentUser =
        CurrentPaymentUser(
            userId = userId,
            telegramId = telegramId,
        )
}
